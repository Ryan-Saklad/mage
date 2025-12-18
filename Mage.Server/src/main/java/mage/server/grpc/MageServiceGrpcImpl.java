package mage.server.grpc;

import io.grpc.stub.StreamObserver;
import mage.MageException;
import mage.cards.decks.DeckCardLists;
import mage.cards.decks.DeckValidatorFactory;
import mage.cards.repository.CardRepository;
import mage.cards.repository.ExpansionRepository;
import mage.constants.ManaType;
import mage.constants.PlayerAction;
import mage.constants.TableState;
import mage.game.Table;
import mage.game.match.MatchOptions;
import mage.game.tournament.TournamentOptions;
import mage.interfaces.callback.ClientCallback;
import mage.interfaces.callback.ClientCallbackMethod;
import mage.players.PlayerType;
import mage.players.net.UserData;
import mage.proto.*;
import mage.server.*;
import mage.server.draft.CubeFactory;
import mage.server.game.GameFactory;
import mage.server.game.GamesRoom;
import mage.server.game.PlayerFactory;
import mage.server.managers.ManagerFactory;
import mage.server.services.impl.FeedbackServiceImpl;
import mage.server.tournament.TournamentFactory;
import mage.server.util.ServerMessagesUtil;
import mage.utils.MageVersion;
import mage.view.*;
import org.apache.log4j.Logger;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.Optional;

/**
 * gRPC implementation of the MageService, replacing JBoss remoting.
 */
public class MageServiceGrpcImpl extends MageServiceGrpc.MageServiceImplBase {

    private static final Logger logger = Logger.getLogger(MageServiceGrpcImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ManagerFactory managerFactory;
    private final String adminPassword;
    private final boolean testMode;
    private final boolean detailsMode;
    private final ExecutorService callExecutor;

    // Active callback streams for each session
    private final Map<String, StreamObserver<ServerCallback>> callbackStreams = new ConcurrentHashMap<>();

    // Auth tokens for password reset
    private final LinkedHashMap<String, String> activeAuthTokens = new LinkedHashMap<String, String>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > 1024;
        }
    };

    public MageServiceGrpcImpl(ManagerFactory managerFactory, String adminPassword, boolean testMode, boolean detailsMode) {
        this.managerFactory = managerFactory;
        this.adminPassword = adminPassword;
        this.testMode = testMode;
        this.detailsMode = detailsMode;
        this.callExecutor = managerFactory.threadExecutor().getCallExecutor();
    }

    private static String generateAuthToken() {
        return String.format("%06d", RANDOM.nextInt(1000000));
    }

    // Auth methods

    @Override
    public void authRegister(AuthRegisterRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            boolean success = managerFactory.sessionManager().registerUser(
                    request.getSessionId(),
                    request.getUserName(),
                    request.getPassword(),
                    request.getEmail()
            );
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(success).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void authSendTokenToEmail(AuthSendTokenRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            String email = request.getEmail();
            String sessionId = request.getSessionId();

            if (!managerFactory.configSettings().isAuthenticationActivated()) {
                responseObserver.onNext(BoolResponse.newBuilder()
                        .setSuccess(false)
                        .setErrorMessage(Session.REGISTRATION_DISABLED_MESSAGE)
                        .build());
                responseObserver.onCompleted();
                return;
            }

            AuthorizedUser authorizedUser = AuthorizedUserRepository.getInstance().getByEmail(email);
            if (authorizedUser == null) {
                responseObserver.onNext(BoolResponse.newBuilder()
                        .setSuccess(false)
                        .setErrorMessage("No user was found with the email address " + email)
                        .build());
                responseObserver.onCompleted();
                return;
            }

            String authToken = generateAuthToken();
            activeAuthTokens.put(email, authToken);
            String subject = "XMage Password Reset Auth Token";
            String text = "Use this auth token to reset " + authorizedUser.getName() + "'s password: " + authToken + '\n'
                    + "It's valid until the next server restart.";

            boolean success;
            if (!managerFactory.configSettings().getMailUser().isEmpty()) {
                success = managerFactory.mailClient().sendMessage(email, subject, text);
            } else {
                success = managerFactory.mailgunClient().sendMessage(email, subject, text);
            }

            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(success).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void authResetPassword(AuthResetPasswordRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            String email = request.getEmail();
            String authToken = request.getAuthToken();
            String password = request.getPassword();

            if (!managerFactory.configSettings().isAuthenticationActivated()) {
                responseObserver.onNext(BoolResponse.newBuilder()
                        .setSuccess(false)
                        .setErrorMessage(Session.REGISTRATION_DISABLED_MESSAGE)
                        .build());
                responseObserver.onCompleted();
                return;
            }

            String storedAuthToken = activeAuthTokens.get(email);
            if (storedAuthToken == null || !storedAuthToken.equals(authToken)) {
                responseObserver.onNext(BoolResponse.newBuilder()
                        .setSuccess(false)
                        .setErrorMessage("Invalid auth token")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            AuthorizedUser authorizedUser = AuthorizedUserRepository.getInstance().getByEmail(email);
            if (authorizedUser == null) {
                responseObserver.onNext(BoolResponse.newBuilder()
                        .setSuccess(false)
                        .setErrorMessage("User with that email doesn't exist")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            AuthorizedUserRepository.getInstance().remove(authorizedUser.getName());
            AuthorizedUserRepository.getInstance().add(authorizedUser.getName(), password, email);
            activeAuthTokens.remove(email);

            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    // Connection methods

    @Override
    public void connectUser(ConnectUserRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            MageVersion version = ProtoConverter.fromProtoVersion(request.getVersion());
            if (version.compareTo(Main.getVersion()) != 0) {
                responseObserver.onNext(BoolResponse.newBuilder()
                        .setSuccess(false)
                        .setErrorMessage("Wrong client version " + version + ", server version is " + Main.getVersion())
                        .build());
                responseObserver.onCompleted();
                return;
            }

            boolean success = managerFactory.sessionManager().connectUser(
                    request.getSessionId(),
                    request.getRestoreSessionId(),
                    request.getUserName(),
                    request.getPassword(),
                    request.getUserIdStr(),
                    this.detailsMode
            );

            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(success).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void connectAdmin(ConnectAdminRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            MageVersion version = ProtoConverter.fromProtoVersion(request.getVersion());
            if (version.compareTo(Main.getVersion()) != 0) {
                responseObserver.onNext(BoolResponse.newBuilder()
                        .setSuccess(false)
                        .setErrorMessage("Wrong client version " + version)
                        .build());
                responseObserver.onCompleted();
                return;
            }

            if (!request.getPassword().equals(this.adminPassword)) {
                Thread.sleep(3000);
                responseObserver.onNext(BoolResponse.newBuilder()
                        .setSuccess(false)
                        .setErrorMessage("Wrong password")
                        .build());
                responseObserver.onCompleted();
                return;
            }

            boolean success = managerFactory.sessionManager().connectAdmin(request.getSessionId());
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(success).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void connectSetUserData(ConnectSetUserDataRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            if (!managerFactory.sessionManager().isValidSession(request.getSessionId())) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            UserData userData = ProtoConverter.fromProtoUserData(request.getUserData());
            boolean success = managerFactory.sessionManager().setUserData(
                    request.getUserName(),
                    request.getSessionId(),
                    userData,
                    request.getClientVersion(),
                    request.getUserIdStr()
            );

            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(success).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void ping(PingRequest request, StreamObserver<BoolResponse> responseObserver) {
        boolean success = managerFactory.sessionManager().extendUserSession(request.getSessionId(), request.getPingInfo());
        responseObserver.onNext(BoolResponse.newBuilder().setSuccess(success).build());
        responseObserver.onCompleted();
    }

    // Server info methods

    @Override
    public void getServerState(mage.proto.Empty request, StreamObserver<mage.proto.ServerStateResponse> responseObserver) {
        try {
            Thread.sleep(1000); // DDoS protection

            mage.proto.ServerStateResponse.Builder builder = mage.proto.ServerStateResponse.newBuilder()
                    .setTestmode(testMode)
                    .setVersion(ProtoConverter.toProtoVersion(Main.getVersion()));

            for (GameTypeView gameType : GameFactory.instance.getGameTypes()) {
                builder.addGameTypes(ProtoConverter.toProtoGameTypeView(gameType));
            }

            for (TournamentTypeView tournamentType : TournamentFactory.instance.getTournamentTypes()) {
                builder.addTournamentTypes(ProtoConverter.toProtoTournamentTypeView(tournamentType));
            }

            for (PlayerType playerType : PlayerFactory.instance.getPlayerTypes()) {
                builder.addPlayerTypes(playerType.toString());
            }

            for (String deckType : DeckValidatorFactory.instance.getDeckTypes()) {
                builder.addDeckTypes(deckType);
            }

            for (String draftCube : CubeFactory.instance.getDraftCubes()) {
                builder.addDraftCubes(draftCube);
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in getServerState", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getMainRoomId(Empty request, StreamObserver<UuidResponse> responseObserver) {
        try {
            UUID roomId = managerFactory.gamesRoomManager().getMainRoomId();
            responseObserver.onNext(UuidResponse.newBuilder().setUuid(ProtoConverter.toProtoUuid(roomId)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getPromotionMessages(SessionRequest request, StreamObserver<PromotionMessagesResponse> responseObserver) {
        try {
            if (!managerFactory.sessionManager().isValidSession(request.getSessionId())) {
                responseObserver.onError(new MageException("Invalid session"));
                return;
            }

            PromotionMessagesResponse.Builder builder = PromotionMessagesResponse.newBuilder();
            for (String msg : ServerMessagesUtil.instance.getMessages()) {
                builder.addMessages(msg);
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    // Feedback methods

    @Override
    public void addFeedbackMessage(FeedbackRequest request, StreamObserver<Empty> responseObserver) {
        try {
            if (request.getTitle() != null && request.getMessage() != null) {
                managerFactory.sessionManager().getSession(request.getSessionId()).ifPresent(session ->
                        FeedbackServiceImpl.instance.feedback(
                                request.getUsername(),
                                request.getTitle(),
                                request.getType(),
                                request.getMessage(),
                                request.getEmail(),
                                session.getHost()
                        )
                );
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    // Room methods

    @Override
    public void roomGetUsers(RoomRequest request, StreamObserver<RoomUsersResponse> responseObserver) {
        try {
            UUID roomId = fromProtoUuid(request.getRoomId());
            Optional<GamesRoom> room = managerFactory.gamesRoomManager().getRoom(roomId);

            RoomUsersResponse.Builder builder = RoomUsersResponse.newBuilder();
            if (room.isPresent()) {
                for (RoomUsersView view : room.get().getRoomUsersInfo()) {
                    builder.addRoomUsers(ProtoConverter.toProtoRoomUsersView(view));
                }
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void roomGetFinishedMatches(RoomRequest request, StreamObserver<MatchListResponse> responseObserver) {
        try {
            UUID roomId = fromProtoUuid(request.getRoomId());
            MatchListResponse.Builder builder = MatchListResponse.newBuilder();

            managerFactory.gamesRoomManager().getRoom(roomId).ifPresent(room -> {
                for (MatchView view : room.getFinished()) {
                    builder.addMatches(ProtoConverter.toProtoMatchView(view));
                }
            });

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void roomGetAllTables(RoomRequest request, StreamObserver<TableListResponse> responseObserver) {
        try {
            UUID roomId = fromProtoUuid(request.getRoomId());
            TableListResponse.Builder builder = TableListResponse.newBuilder();

            managerFactory.gamesRoomManager().getRoom(roomId).ifPresent(room -> {
                for (TableView view : room.getTables()) {
                    builder.addTables(ProtoConverter.toProtoTableView(view));
                }
            });

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void roomGetTableById(GetTableRequest request, StreamObserver<TableViewProto> responseObserver) {
        try {
            UUID roomId = fromProtoUuid(request.getRoomId());
            UUID tableId = fromProtoUuid(request.getTableId());

            Optional<GamesRoom> room = managerFactory.gamesRoomManager().getRoom(roomId);
            if (room.isPresent()) {
                Optional<TableView> table = room.get().getTable(tableId);
                if (table.isPresent()) {
                    responseObserver.onNext(ProtoConverter.toProtoTableView(table.get()));
                    responseObserver.onCompleted();
                    return;
                }
            }
            responseObserver.onNext(TableViewProto.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void roomCreateTable(CreateTableRequest request, StreamObserver<TableViewProto> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(TableViewProto.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
                responseObserver.onNext(TableViewProto.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            UUID userId = session.get().getUserId();
            UUID roomId = fromProtoUuid(request.getRoomId());
            MatchOptions options = fromProtoMatchOptions(request.getMatchOptions());

            Optional<GamesRoom> room = managerFactory.gamesRoomManager().getRoom(roomId);
            if (room.isPresent()) {
                TableView table = room.get().createTable(userId, options);
                responseObserver.onNext(ProtoConverter.toProtoTableView(table));
            } else {
                responseObserver.onNext(TableViewProto.getDefaultInstance());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error creating table", e);
            responseObserver.onNext(TableViewProto.getDefaultInstance());
            responseObserver.onCompleted();
        }
    }

    private MatchOptions fromProtoMatchOptions(MatchOptionsProto proto) {
        MatchOptions options = new MatchOptions(proto.getName(), proto.getGameType(), false, proto.getNumSeats());
        options.setDeckType(proto.getDeckType());
        options.setLimited(proto.getLimited());
        options.setRated(proto.getRated());
        options.setSkillLevel(toSkillLevel(proto.getSkillLevel()));
        options.setWinsNeeded(proto.getWinsNeeded());
        options.setFreeMulligans(proto.getFreeMulligan());
        options.setPassword(proto.getPassword());
        options.setRollbackTurnsAllowed(proto.getRollbackTurnsAllowed());
        options.setSpectatorsAllowed(proto.getSpectatorsAllowed());
        options.setPlaneChase(proto.getPlaneChase());
        options.setQuitRatio(proto.getQuitRatio());
        options.setMinimumRating(proto.getMinimumRating());
        options.setEdhPowerLevel(proto.getEdhPowerLevel());
        options.setPriorityTime(proto.getPriorityTime());
        return options;
    }

    @Override
    public void roomCreateTournament(CreateTournamentRequest request, StreamObserver<TableViewProto> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(TableViewProto.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
                responseObserver.onNext(TableViewProto.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            UUID userId = session.get().getUserId();
            UUID roomId = fromProtoUuid(request.getRoomId());
            TournamentOptions options = fromProtoTournamentOptions(request.getTournamentOptions());

            Optional<GamesRoom> room = managerFactory.gamesRoomManager().getRoom(roomId);
            if (room.isPresent()) {
                TableView table = room.get().createTournamentTable(userId, options);
                responseObserver.onNext(ProtoConverter.toProtoTableView(table));
            } else {
                responseObserver.onNext(TableViewProto.getDefaultInstance());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error creating tournament", e);
            responseObserver.onNext(TableViewProto.getDefaultInstance());
            responseObserver.onCompleted();
        }
    }

    private TournamentOptions fromProtoTournamentOptions(TournamentOptionsProto proto) {
        TournamentOptions options = new TournamentOptions(proto.getName());
        options.setTournamentType(proto.getTournamentType());
        options.getMatchOptions().setDeckType(proto.getDeckType());
        options.getMatchOptions().setLimited(proto.getLimited());
        options.getMatchOptions().setRated(proto.getRated());
        options.getMatchOptions().setSkillLevel(toSkillLevel(proto.getSkillLevel()));
        options.setNumberSeats(proto.getNumSeats());
        options.setPassword(proto.getPassword());
        options.setWatchingAllowed(proto.getWatchAllowed());
        options.setQuitRatio(proto.getQuitRatio());
        options.setMinimumRating(proto.getMinimumRating());
        return options;
    }

    @Override
    public void roomJoinTable(JoinTableRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            UUID userId = session.get().getUserId();
            UUID roomId = fromProtoUuid(request.getRoomId());
            UUID tableId = fromProtoUuid(request.getTableId());
            String name = request.getName();
            PlayerType playerType = toPlayerType(request.getPlayerType());
            int skill = request.getSkill();
            DeckCardLists deckList = fromProtoDeckCardLists(request.getDeckList());
            String password = request.getPassword();

            Optional<GamesRoom> room = managerFactory.gamesRoomManager().getRoom(roomId);
            if (room.isPresent()) {
                boolean success = room.get().joinTable(userId, tableId, name, playerType, skill, deckList, password);
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(success).build());
            } else {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void roomJoinTournament(JoinTournamentRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            UUID userId = session.get().getUserId();
            UUID roomId = fromProtoUuid(request.getRoomId());
            UUID tableId = fromProtoUuid(request.getTableId());
            String name = request.getName();
            PlayerType playerType = toPlayerType(request.getPlayerType());
            int skill = request.getSkill();
            DeckCardLists deckList = fromProtoDeckCardLists(request.getDeckList());
            String password = request.getPassword();

            Optional<GamesRoom> room = managerFactory.gamesRoomManager().getRoom(roomId);
            if (room.isPresent()) {
                boolean success = room.get().joinTournamentTable(userId, tableId, name, playerType, skill, deckList, password);
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(success).build());
            } else {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void roomWatchTable(WatchTableRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            UUID userId = session.get().getUserId();
            UUID roomId = fromProtoUuid(request.getRoomId());
            UUID tableId = fromProtoUuid(request.getTableId());

            Optional<GamesRoom> room = managerFactory.gamesRoomManager().getRoom(roomId);
            if (room.isPresent()) {
                boolean success = room.get().watchTable(userId, tableId);
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(success).build());
            } else {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void roomWatchTournament(WatchTournamentRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            UUID userId = session.get().getUserId();
            UUID tableId = fromProtoUuid(request.getTableId());

            boolean success = managerFactory.tableManager().watchTable(userId, tableId);
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(success).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void roomLeaveTableOrTournament(LeaveTableRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            UUID roomId = fromProtoUuid(request.getRoomId());
            UUID tableId = fromProtoUuid(request.getTableId());

            Optional<TableController> tableController = managerFactory.tableManager().getController(tableId);
            if (tableController.isPresent()) {
                TableState tableState = tableController.get().getTableState();
                if (tableState != TableState.WAITING && tableState != TableState.READY_TO_START) {
                    responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                    responseObserver.onCompleted();
                    return;
                }

                managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                    UUID userId = session.getUserId();
                    managerFactory.gamesRoomManager().getRoom(roomId).ifPresent(room ->
                            room.leaveTable(userId, tableId));
                });
            }

            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    // Table methods

    @Override
    public void tableSwapSeats(SwapSeatsRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                UUID tableId = fromProtoUuid(request.getTableId());
                managerFactory.tableManager().swapSeats(tableId, userId, request.getSeatNum1(), request.getSeatNum2());
            });

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void tableRemove(RemoveTableRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                UUID tableId = fromProtoUuid(request.getTableId());
                managerFactory.tableManager().removeTable(userId, tableId);
            });

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void tableIsOwner(TableOwnerRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            UUID userId = session.get().getUserId();
            UUID tableId = fromProtoUuid(request.getTableId());
            boolean isOwner = managerFactory.tableManager().isTableOwner(tableId, userId);

            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(isOwner).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    // Deck methods

    @Override
    public void deckSubmit(DeckSubmitRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            UUID userId = session.get().getUserId();
            UUID tableId = fromProtoUuid(request.getTableId());
            DeckCardLists deckList = fromProtoDeckCardLists(request.getDeckList());

            boolean success = managerFactory.tableManager().submitDeck(userId, tableId, deckList);
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(success).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deckSave(DeckSaveRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (session.isPresent()) {
                UUID userId = session.get().getUserId();
                UUID tableId = fromProtoUuid(request.getTableId());
                DeckCardLists deckList = fromProtoDeckCardLists(request.getDeckList());
                managerFactory.tableManager().updateDeck(userId, tableId, deckList);
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    // Chat methods

    @Override
    public void chatSendMessage(ChatSendRequest request, StreamObserver<Empty> responseObserver) {
        try {
            UUID chatId = fromProtoUuid(request.getChatId());
            String message = request.getMessage();

            if (message.length() > mage.constants.Constants.MAX_CHAT_MESSAGE_SIZE) {
                logger.error("Chat message too big: " + message.length());
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            callExecutor.execute(() ->
                    managerFactory.chatManager().broadcast(
                            chatId,
                            request.getUserName(),
                            org.unbescape.html.HtmlEscape.escapeHtml4(message),
                            ChatMessage.MessageColor.BLUE,
                            true,
                            null,
                            ChatMessage.MessageType.TALK,
                            null
                    )
            );

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void chatJoin(ChatJoinRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                UUID chatId = fromProtoUuid(request.getChatId());
                managerFactory.chatManager().joinChat(chatId, userId);
            });

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void chatLeave(ChatLeaveRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            UUID chatId = fromProtoUuid(request.getChatId());

            if (chatId != null) {
                managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                    UUID userId = session.getUserId();
                    managerFactory.chatManager().leaveChat(chatId, userId);
                });
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void chatFindByGame(UuidRequest request, StreamObserver<UuidResponse> responseObserver) {
        try {
            UUID gameId = fromProtoUuid(request.getUuid());
            UUID chatId = managerFactory.gameManager().getChatId(gameId).orElse(null);
            responseObserver.onNext(UuidResponse.newBuilder().setUuid(ProtoConverter.toProtoUuid(chatId)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void chatFindByTable(UuidRequest request, StreamObserver<UuidResponse> responseObserver) {
        try {
            UUID tableId = fromProtoUuid(request.getUuid());
            UUID chatId = managerFactory.tableManager().getChatId(tableId).orElse(null);
            responseObserver.onNext(UuidResponse.newBuilder().setUuid(ProtoConverter.toProtoUuid(chatId)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void chatFindByTournament(UuidRequest request, StreamObserver<UuidResponse> responseObserver) {
        try {
            UUID tournamentId = fromProtoUuid(request.getUuid());
            UUID chatId = managerFactory.tournamentManager().getChatId(tournamentId).orElse(null);
            responseObserver.onNext(UuidResponse.newBuilder().setUuid(ProtoConverter.toProtoUuid(chatId)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void chatFindByRoom(UuidRequest request, StreamObserver<UuidResponse> responseObserver) {
        try {
            UUID roomId = fromProtoUuid(request.getUuid());
            Optional<GamesRoom> room = managerFactory.gamesRoomManager().getRoom(roomId);
            UUID chatId = room.map(GamesRoom::getChatId).orElse(null);
            responseObserver.onNext(UuidResponse.newBuilder().setUuid(ProtoConverter.toProtoUuid(chatId)).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    // Match methods

    @Override
    public void matchStart(MatchStartRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            UUID roomId = fromProtoUuid(request.getRoomId());
            UUID tableId = fromProtoUuid(request.getTableId());

            Optional<TableController> controller = managerFactory.tableManager().getController(tableId);
            if (!controller.isPresent()) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            if (!controller.get().changeTableStateToStarting()) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            callExecutor.execute(() -> {
                managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                    UUID userId = session.getUserId();
                    managerFactory.tableManager().startMatch(userId, roomId, tableId);
                });
            });

            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void matchQuit(MatchQuitRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            UUID gameId = fromProtoUuid(request.getGameId());

            callExecutor.execute(() -> {
                managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                    UUID userId = session.getUserId();
                    managerFactory.gameManager().quitMatch(gameId, userId);
                });
            });

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    // Game methods

    @Override
    public void gameJoin(GameSessionRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                UUID gameId = fromProtoUuid(request.getGameId());
                managerFactory.gameManager().joinGame(gameId, userId);
            });

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void gameGetView(GameGetViewRequest request, StreamObserver<GameViewProto> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(GameViewProto.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            UUID gameId = fromProtoUuid(request.getGameId());
            UUID playerId = fromProtoUuid(request.getPlayerId());

            GameView view = managerFactory.gameManager().getGameView(gameId, playerId);
            responseObserver.onNext(toProtoGameView(view));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void gameWatchStart(GameSessionRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            UUID userId = session.get().getUserId();
            UUID gameId = fromProtoUuid(request.getGameId());

            boolean success = managerFactory.gameManager().watchGame(gameId, userId);
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(success).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void gameWatchStop(GameSessionRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();

            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                UUID gameId = fromProtoUuid(request.getGameId());

                managerFactory.userManager().getUser(userId).ifPresent(user -> {
                    managerFactory.gameManager().stopWatching(gameId, userId);
                    user.removeGameWatchInfo(gameId);
                });
            });

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    // Player input methods

    @Override
    public void sendPlayerUUID(PlayerUuidRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            Optional<User> user = managerFactory.sessionManager().getUser(sessionId);
            if (user.isPresent()) {
                UUID gameId = fromProtoUuid(request.getGameId());
                UUID data = fromProtoUuid(request.getData());
                user.get().sendPlayerUUID(gameId, data);
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void sendPlayerString(PlayerStringRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            Optional<User> user = managerFactory.sessionManager().getUser(sessionId);
            if (user.isPresent()) {
                UUID gameId = fromProtoUuid(request.getGameId());
                user.get().sendPlayerString(gameId, request.getData());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void sendPlayerBoolean(PlayerBooleanRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            Optional<User> user = managerFactory.sessionManager().getUser(sessionId);
            if (user.isPresent()) {
                UUID gameId = fromProtoUuid(request.getGameId());
                user.get().sendPlayerBoolean(gameId, request.getData());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void sendPlayerInteger(PlayerIntegerRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            Optional<User> user = managerFactory.sessionManager().getUser(sessionId);
            if (user.isPresent()) {
                UUID gameId = fromProtoUuid(request.getGameId());
                user.get().sendPlayerInteger(gameId, request.getData());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void sendPlayerManaType(PlayerManaTypeRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            Optional<User> user = managerFactory.sessionManager().getUser(sessionId);
            if (user.isPresent()) {
                UUID gameId = fromProtoUuid(request.getGameId());
                UUID playerId = fromProtoUuid(request.getPlayerId());
                ManaType manaType = toManaType(request.getData());
                user.get().sendPlayerManaType(gameId, playerId, manaType);
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void sendPlayerAction(PlayerActionRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                UUID gameId = fromProtoUuid(request.getGameId());
                PlayerAction action = toPlayerAction(request.getAction());
                // data is serialized in bytes - for now pass null, will need to handle specific actions
                managerFactory.gameManager().sendPlayerAction(action, gameId, userId, null);
            });

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    // Draft methods

    @Override
    public void sendDraftCardPick(DraftPickRequest request, StreamObserver<DraftPickViewProto> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(DraftPickViewProto.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (!session.isPresent()) {
                responseObserver.onNext(DraftPickViewProto.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            UUID draftId = fromProtoUuid(request.getDraftId());
            UUID cardId = fromProtoUuid(request.getCardId());
            Set<UUID> hiddenCards = new HashSet<>();
            for (String id : request.getHiddenCardsList()) {
                hiddenCards.add(fromProtoUuid(id));
            }

            DraftPickView view = managerFactory.draftManager().sendCardPick(draftId, session.get().getUserId(), cardId, hiddenCards);
            responseObserver.onNext(toProtoDraftPickView(view));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void sendDraftCardMark(DraftMarkRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                UUID draftId = fromProtoUuid(request.getDraftId());
                UUID cardId = fromProtoUuid(request.getCardId());
                managerFactory.draftManager().sendCardMark(draftId, userId, cardId);
            });

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void draftJoin(DraftSessionRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                UUID draftId = fromProtoUuid(request.getDraftId());
                managerFactory.draftManager().joinDraft(draftId, userId);
            });

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void draftQuit(DraftSessionRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();

            callExecutor.execute(() -> {
                managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                    UUID userId = session.getUserId();
                    UUID draftId = fromProtoUuid(request.getDraftId());
                    UUID tableId = managerFactory.draftManager().getControllerByDraftId(draftId).getTableId();
                    Table table = managerFactory.tableManager().getTable(tableId);
                    if (table.isTournament()) {
                        UUID tournamentId = table.getTournament().getId();
                        managerFactory.tournamentManager().quit(tournamentId, userId);
                    }
                });
            });

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void draftSetBoosterLoaded(DraftSessionRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                UUID draftId = fromProtoUuid(request.getDraftId());
                managerFactory.draftManager().setBoosterLoaded(draftId, userId);
            });

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    // Tournament methods

    @Override
    public void tournamentStart(TournamentStartRequest request, StreamObserver<BoolResponse> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            UUID roomId = fromProtoUuid(request.getRoomId());
            UUID tableId = fromProtoUuid(request.getTableId());

            Optional<TableController> controller = managerFactory.tableManager().getController(tableId);
            if (!controller.isPresent()) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            if (!controller.get().changeTableStateToStarting()) {
                responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).build());
                responseObserver.onCompleted();
                return;
            }

            callExecutor.execute(() -> {
                managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                    UUID userId = session.getUserId();
                    managerFactory.tableManager().startTournament(userId, roomId, tableId);
                });
            });

            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BoolResponse.newBuilder().setSuccess(false).setErrorMessage(e.getMessage()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void tournamentJoin(TournamentSessionRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (session.isPresent()) {
                UUID userId = session.get().getUserId();
                UUID tournamentId = fromProtoUuid(request.getTournamentId());
                managerFactory.tournamentManager().joinTournament(tournamentId, userId);
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void tournamentQuit(TournamentSessionRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();

            callExecutor.execute(() -> {
                managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                    UUID userId = session.getUserId();
                    UUID tournamentId = fromProtoUuid(request.getTournamentId());
                    managerFactory.tournamentManager().quit(tournamentId, userId);
                });
            });

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void tournamentFindById(UuidRequest request, StreamObserver<TournamentViewProto> responseObserver) {
        try {
            UUID tournamentId = fromProtoUuid(request.getUuid());
            TournamentView view = managerFactory.tournamentManager().getTournamentView(tournamentId);
            responseObserver.onNext(toProtoTournamentView(view));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    // Replay methods

    @Override
    public void replayInit(GameSessionRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                UUID gameId = fromProtoUuid(request.getGameId());
                managerFactory.replayManager().replayGame(gameId, userId);
            });

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void replayStart(GameSessionRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                UUID userId = session.getUserId();
                UUID gameId = fromProtoUuid(request.getGameId());
                managerFactory.replayManager().startReplay(gameId, userId);
            });

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void replayStop(GameSessionRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (session.isPresent()) {
                UUID userId = session.get().getUserId();
                UUID gameId = fromProtoUuid(request.getGameId());
                managerFactory.replayManager().stopReplay(gameId, userId);
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void replayNext(GameSessionRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (session.isPresent()) {
                UUID userId = session.get().getUserId();
                UUID gameId = fromProtoUuid(request.getGameId());
                managerFactory.replayManager().nextPlay(gameId, userId);
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void replayPrevious(GameSessionRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (session.isPresent()) {
                UUID userId = session.get().getUserId();
                UUID gameId = fromProtoUuid(request.getGameId());
                managerFactory.replayManager().previousPlay(gameId, userId);
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void replaySkipForward(ReplaySkipRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();

            Optional<Session> session = managerFactory.sessionManager().getSession(sessionId);
            if (session.isPresent()) {
                UUID userId = session.get().getUserId();
                UUID gameId = fromProtoUuid(request.getGameId());
                managerFactory.replayManager().skipForward(gameId, userId, request.getMoves());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    // Cheat methods

    @Override
    public void cheatShow(CheatShowRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().isValidSession(sessionId)) {
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            if (testMode) {
                managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                    UUID userId = session.getUserId();
                    UUID gameId = fromProtoUuid(request.getGameId());
                    UUID playerId = fromProtoUuid(request.getPlayerId());
                    managerFactory.gameManager().cheatShow(gameId, userId, playerId);
                });
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    // Admin methods

    @Override
    public void adminGetUsers(SessionRequest request, StreamObserver<UserListResponse> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (!managerFactory.sessionManager().checkAdminAccess(sessionId)) {
                responseObserver.onNext(UserListResponse.getDefaultInstance());
                responseObserver.onCompleted();
                return;
            }

            UserListResponse.Builder builder = UserListResponse.newBuilder();
            for (UserView view : managerFactory.userManager().getUserInfoList()) {
                builder.addUsers(toProtoUserView(view));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void adminDisconnectUser(AdminUserRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (managerFactory.sessionManager().checkAdminAccess(sessionId)) {
                managerFactory.sessionManager().disconnectAnother(sessionId, request.getUserSessionId());
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void adminEndUserSession(AdminUserRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (managerFactory.sessionManager().checkAdminAccess(sessionId)) {
                managerFactory.sessionManager().disconnectAnother(sessionId, request.getUserSessionId());
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void adminMuteUser(AdminMuteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (managerFactory.sessionManager().checkAdminAccess(sessionId)) {
                managerFactory.userManager().getUserByName(request.getUserName()).ifPresent(user -> {
                    Date muteUntil = new Date(Calendar.getInstance().getTimeInMillis() + (request.getDurationMinutes() * 60 * 1000));
                    user.showUserMessage("Admin info", "You were muted for chat messages until " + muteUntil + '.');
                    user.setChatLockedUntil(muteUntil);
                });
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void adminLockUser(AdminLockRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (managerFactory.sessionManager().checkAdminAccess(sessionId)) {
                managerFactory.userManager().getUserByName(request.getUserName()).ifPresent(user -> {
                    Date lockUntil = new Date(Calendar.getInstance().getTimeInMillis() + (request.getDurationMinutes() * 60 * 1000));
                    user.showUserMessage("Admin info", "Your user profile was locked until " + lockUntil + '.');
                    user.setLockedUntil(lockUntil);
                    if (user.isConnected()) {
                        managerFactory.sessionManager().disconnectAnother(sessionId, user.getSessionId());
                    }
                });
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void adminActivateUser(AdminActivateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (managerFactory.sessionManager().checkAdminAccess(sessionId)) {
                String userName = request.getUserName();
                boolean active = request.getActive();

                AuthorizedUser authorizedUser = AuthorizedUserRepository.getInstance().getByName(userName);
                Optional<User> u = managerFactory.userManager().getUserByName(userName);
                if (u.isPresent()) {
                    User user = u.get();
                    user.setActive(active);
                    if (!user.isActive() && user.isConnected()) {
                        managerFactory.sessionManager().disconnectAnother(sessionId, user.getSessionId());
                    }
                } else if (authorizedUser != null) {
                    User theUser = new User(managerFactory, userName, "localhost", authorizedUser);
                    theUser.setActive(active);
                }
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void adminToggleActivateUser(AdminToggleActivateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (managerFactory.sessionManager().checkAdminAccess(sessionId)) {
                managerFactory.userManager().getUserByName(request.getUserName()).ifPresent(user -> {
                    user.setActive(!user.isActive());
                    if (!user.isActive() && user.isConnected()) {
                        managerFactory.sessionManager().disconnectAnother(sessionId, user.getSessionId());
                    }
                });
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void adminTableRemove(AdminTableRemoveRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            if (managerFactory.sessionManager().checkAdminAccess(sessionId)) {
                managerFactory.sessionManager().getSession(sessionId).ifPresent(session -> {
                    UUID userId = session.getUserId();
                    UUID tableId = fromProtoUuid(request.getTableId());
                    managerFactory.tableManager().removeTable(userId, tableId);
                });
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void adminSendBroadcastMessage(AdminBroadcastRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String sessionId = request.getSessionId();
            String message = request.getMessage();

            if (managerFactory.sessionManager().checkAdminAccess(sessionId) && message != null) {
                for (User user : managerFactory.userManager().getUsers()) {
                    ChatMessage.MessageColor color = message.toLowerCase(Locale.ENGLISH).startsWith("warn")
                            ? ChatMessage.MessageColor.RED
                            : ChatMessage.MessageColor.BLUE;
                    user.fireCallback(new ClientCallback(
                            ClientCallbackMethod.SERVER_MESSAGE,
                            null,
                            new ChatMessage("SERVER", message, null, null, color)
                    ));
                }
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    // Callback streaming

    @Override
    public void subscribeToCallbacks(SubscribeRequest request, StreamObserver<ServerCallback> responseObserver) {
        String sessionId = request.getSessionId();
        callbackStreams.put(sessionId, responseObserver);

        // The stream stays open until the client disconnects
        // When the client disconnects, we'll get an error on the responseObserver
    }

    /**
     * Send a callback to a specific session.
     * This method is called by the callback system to push events to clients.
     */
    public void sendCallback(String sessionId, ServerCallback callback) {
        StreamObserver<ServerCallback> observer = callbackStreams.get(sessionId);
        if (observer != null) {
            try {
                observer.onNext(callback);
            } catch (Exception e) {
                logger.error("Error sending callback to session " + sessionId, e);
                callbackStreams.remove(sessionId);
            }
        }
    }

    /**
     * Remove a callback stream when the client disconnects.
     */
    public void removeCallbackStream(String sessionId) {
        StreamObserver<ServerCallback> observer = callbackStreams.remove(sessionId);
        if (observer != null) {
            try {
                observer.onCompleted();
            } catch (Exception e) {
                // Client already disconnected
            }
        }
    }
}
