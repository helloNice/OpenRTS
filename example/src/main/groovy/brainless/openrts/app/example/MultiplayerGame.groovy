package brainless.openrts.app.example;

import groovy.transform.CompileStatic

import java.util.logging.Logger

import tonegod.gui.core.Screen
import tonegod.gui.tests.states.TestState
import tonegod.gui.tests.states.buttons.ButtonState
import tonegod.gui.tests.states.emitter.EmitterState
import tonegod.gui.tests.states.spatial.SpatialState
import tonegod.gui.tests.states.sprite.SpriteState
import tonegod.gui.tests.states.subscreen.EmbeddedGUIState 
import tonegod.gui.tests.states.text.AnimatedTextState
import tonegod.gui.tests.states.text.TextLabelState
import tonegod.gui.tests.states.windows.WindowState
import app.OpenRTSApplicationWithDI
import brainless.openrts.app.example.states.NetworkClientState
import brainless.openrts.app.example.states.gui.DashboardState
import brainless.openrts.app.example.states.gui.LoadingMapState
import brainless.openrts.app.example.states.gui.UserLoginAppState
import brainless.openrts.app.example.states.gui.game.BattlefieldState
import brainless.openrts.app.example.states.gui.game.HudState
import brainless.openrts.app.example.states.gui.network.GameLobbyState
import brainless.openrts.app.example.states.gui.network.NetworkDashboardState
import brainless.openrts.app.example.states.gui.network.OpenGameState
import brainless.openrts.model.Game

import com.google.inject.Module
import com.jme3.font.BitmapFont
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.renderer.RenderManager
import com.jme3.system.AppSettings


@CompileStatic
public class MultiplayerGame extends OpenRTSApplicationWithDI {

	private static final Logger logger = Logger.getLogger(MultiplayerGame.class.getName());
	
	//<editor-fold desc="VARIABLES">
	// Config settings for initial load
	// Library default theme
	public static final String BASE_THEME_PATH = "tonegod/gui/style/";
	public static final String DEFAULT_PATH = "def/";
	public static final String ATLAS_PATH = "atlasdef/";
	public static final String ASSET_PATH = "atlasdef/";
	// Theme pack
	//	public static final String BASE_THEME_PATH = "tonegod/gui/themes/fallout/";
	//	public static final String DEFAULT_PATH = "";
	//	public static final String ATLAS_PATH = "atlas/";
	//	public static final String ASSET_PATH = "assets/";
	// Initial GUI Extras settings
	public static final boolean USE_ATLAS = true;
	public static final boolean USE_UI_AUDIO = true;
	String localUser

	private boolean fixCam = false;
	private boolean hasLights = false;
	private DirectionalLight dl;
	private AmbientLight al;

	// GUI Variables
	private Screen screen;
	private BitmapFont defaultFont;

	// States
	//	private List<AppStateCommon> states = new ArrayList(); 
	private NetworkDashboardState networkDashboardState;

	BattlefieldState gameState
	HudState hudState
	LoadingMapState loadingMapState
	DashboardState dashboardState
	GameLobbyState gameLobbyState
	OpenGameState openGameState

	UserLoginAppState userlogin;
	private TestState tests;
	private WindowState winState;
	private SpriteState spriteState;
	private AnimatedTextState animatedTextState;
	private TextLabelState labelState;
	private ButtonState buttonState;
	private EmitterState emitterState;
	private EmbeddedGUIState subScreenState;
	private SpatialState spatialState; 

	Game game

	public static void main(String[] args) {
		// Properties preferences = new Properties();
		// try {
		// FileInputStream configFile = new FileInputStream("logging.properties");
		// preferences.load(configFile);
		// LogManager.getLogManager().readConfiguration(configFile);
		// } catch (IOException ex) {
		// System.err.println("WARNING: Could not open configuration file - please create a logging.properties for correct logging");
		// System.err.println("WARNING: Logging not configured (console output only)");
		// }

		AppSettings settings = new AppSettings(true);
		settings.setResolution(800,600);

		MultiplayerGame app = new MultiplayerGame();
		app.setSettings(settings);
		app.start();
	}


	@Override
	public void simpleInitApp() {
		initScreen();
		List<Module> modules = new ArrayList<Module>([
			new ClientModule(app: this, screen: screen)
		])
		initGuice(modules);
		initLights();
		game = new Game();
		userlogin = injector.getInstance(UserLoginAppState.class);
		stateManager.attach(userlogin);
		pauseOnLostFocus = false
	}

	private void initScreen() {
		screen = new Screen(this, BASE_THEME_PATH + (USE_ATLAS ? ATLAS_PATH : DEFAULT_PATH) + "style_map.gui.xml");

		if (USE_ATLAS) {
			screen.setUseTextureAtlas(true, BASE_THEME_PATH + ASSET_PATH + "atlas.png");
		}
		screen.setUseUIAudio(USE_UI_AUDIO);
		screen.setUseUIAudio(true);
		screen.setUseCustomCursors(true);
		screen.setUseCursorEffects(true);
		screen.setUseToolTips(true);
		guiNode.addControl(screen);

		defaultFont = getAssetManager().loadFont(screen.getStyle("Font").getString("defaultFont"));

		inputManager.setCursorVisible(true);
	}

	public Screen getScreen() {
		return this.screen;
	}

	public BitmapFont getFont() {
		return this.defaultFont;
	}

	//	public List<AppStateCommon> getStates() { return states; }

	private void initLights() {
		al = new AmbientLight();
		al.setColor(new ColorRGBA(0.25f,0.25f,0.25f,1f));

		dl = new DirectionalLight();
		dl.setDirection(new Vector3f(1f,-1f,-1f).normalizeLocal());
		dl.setColor(ColorRGBA.White);
	}

	public void addSceneLights() {
		if (!hasLights) {
			rootNode.addLight(al);
			rootNode.addLight(dl);
			hasLights = true;
		}
	}

	public void removeSceneLights() {
		if (hasLights) {
			rootNode.removeLight(al);
			rootNode.removeLight(dl);
			hasLights = false;
		}
	}

	@Override
	public void reshape(int w, int h) {
		super.reshape(w, h);
		//		for (AppStateCommon state : states) {
		//			state.reshape();
		//		}
	}

	@Override
	public void simpleUpdate(float tpf) {

	}

	@Override
	public void simpleRender(RenderManager rm) {  }

	def sucessfullLoggedIn(String user) {
		stateManager.detach(userlogin);
		dashboardState = injector.getInstance(DashboardState.class);
		this.localUser = user;
		//		states.add(serverConfig);
		stateManager.attach(dashboardState);
	}

	def loadMap() {
		stateManager.detach(networkDashboardState)
		userlogin.enabled = false

		loadingMapState = injector.getInstance(LoadingMapState.class);
		//		states.add(loadingMapState)
		stateManager.attach(loadingMapState)
	}

	def createGame(){
		stateManager.detach(networkDashboardState)

		openGameState = injector.getInstance(OpenGameState.class);
		stateManager.attach(openGameState)
	}

	def openGame(){
		stateManager.detach(gameLobbyState)

		loadingMapState = injector.getInstance(LoadingMapState.class);
		//		states.add(loadingMapState)
		stateManager.attach(loadingMapState)

	}

	def joinGame(){
		stateManager.detach(networkDashboardState)
		gameLobbyState = injector.getInstance(GameLobbyState.class);
		stateManager.attach(gameLobbyState)
	}

	def startGame() {
		stateManager.detach(loadingMapState)
		loadingMapState.setEnabled(false)

		gameState = injector.getInstance(BattlefieldState.class);
		//		states.add(gameState);
		stateManager.attach(gameState);

		hudState = injector.getInstance(HudState.class);
		//		states.add(gameHudState);
		stateManager.attach(hudState);

	}

	def connectToServer(String host) {
		def client = injector.getInstance(NetworkClientState.class);
		client.host = host
		stateManager.attach(client);

		stateManager.detach(networkDashboardState)
		userlogin.enabled = false

		networkDashboardState = injector.getInstance(NetworkDashboardState.class);
		//		states.add(loadingMapState)
		stateManager.attach(networkDashboardState)

	}


	@Override
	public void destroy() {
		super.destroy();
		this.stop();
	}

}
