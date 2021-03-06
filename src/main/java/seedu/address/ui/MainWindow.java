package seedu.address.ui;

import java.util.logging.Logger;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.MainApp;
import seedu.address.commons.core.Config;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.ui.ChangeInformationPanelRequestEvent;
import seedu.address.commons.events.ui.ExitAppRequestEvent;
import seedu.address.commons.util.FxViewUtil;
import seedu.address.logic.Logic;
import seedu.address.model.UserPrefs;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Region> {

    private static final String ICON = "/images/address_book_32.png";
    private static final String FXML = "MainWindow.fxml";
    private static final String HOME_PANEL = "HomePanel";
    private static final String BIRTHDAY_STATISTICS_PANEL = "BirthdayStatisticsPanel";
    private static final String PERSON_INFORMATION_PANEL = "PersonInformationPanel";
    private static final String HELP_PANEL = "HelpPanel";
    private static final String TAG_STATISTICS_PANEL = "TagStatisticsPanel";
    private static final int MIN_HEIGHT = 800;
    private static final int MIN_WIDTH = 1500;

    private final Logger logger = LogsCenter.getLogger(this.getClass());

    private Stage primaryStage;
    private Logic logic;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private HomePanel homePanel;
    private PersonInformationPanel personInformationPanel;
    private HelpPanel helpPanel;
    private BirthdayStatisticsPanel birthdayStatisticsPanel;
    private TagStatisticsPanel tagStatisticsPanel;
    private Config config;
    private UserPrefs prefs;
    private String currentInformationPanel;

    @FXML
    private StackPane informationPanelPlaceholder;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem homeMenuItem;

    @FXML
    private MenuItem birthdayStatisticsMenuItem;

    @FXML
    private MenuItem tagStatisticsMenuItem;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    public MainWindow(Stage primaryStage, Config config, UserPrefs prefs, Logic logic) {
        super(FXML);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.config = config;
        this.prefs = prefs;

        // Configure the UI
        setTitle(config.getAppTitle());
        setIcon(ICON);
        setWindowMinSize();
        setWindowDefaultSize(prefs);
        Scene scene = new Scene(getRoot());
        primaryStage.setScene(scene);

        setAccelerators();
        registerAsAnEventHandler(this);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
        setAccelerator(homeMenuItem, KeyCombination.valueOf("F2"));
        setAccelerator(birthdayStatisticsMenuItem, KeyCombination.valueOf("F3"));
        setAccelerator(tagStatisticsMenuItem, KeyCombination.valueOf("F4"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator.
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in later version of SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField, and
         * ResultDisplay contains a TextArea, thus some accelerators (e.g F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add following event filter to capture such key events and open
         * help window purposely so to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    // @@author johnweikangong
    /**
     * Fills up all the placeholders of this window.
     */
    public void fillInnerParts() {
        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        ResultDisplay resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(prefs.getAddressBookFilePath());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(logic);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());

        /** At start, Initalise all dynamic information panels for MainWindowHandle
         * to be able to initalise these panel in its respective handles for testing. */
        personInformationPanel = new PersonInformationPanel();
        informationPanelPlaceholder.getChildren().add(personInformationPanel.getRoot());

        helpPanel = new HelpPanel();
        informationPanelPlaceholder.getChildren().add(helpPanel.getRoot());

        birthdayStatisticsPanel = new BirthdayStatisticsPanel(logic.getAddressBook());
        informationPanelPlaceholder.getChildren().add(birthdayStatisticsPanel.getRoot());

        tagStatisticsPanel = new TagStatisticsPanel(logic.getAddressBook());
        informationPanelPlaceholder.getChildren().add(tagStatisticsPanel.getRoot());

        homePanel = new HomePanel(logic.getAddressBook());
        informationPanelPlaceholder.getChildren().add(homePanel.getRoot());
    }

    /** Changes the information panel based on request event. */
    public void changeInformationPanel(ChangeInformationPanelRequestEvent event) {
        if (event.getPanelRequestEvent().equals(currentInformationPanel)) {
            return; // Short circuit if the current information panel is the same as the requested information panel
        } else {
            informationPanelPlaceholder.getChildren().removeAll(homePanel.getRoot(), personInformationPanel.getRoot(),
                    helpPanel.getRoot(), birthdayStatisticsPanel.getRoot(), tagStatisticsPanel.getRoot());

            if (event.getPanelRequestEvent().equals(PERSON_INFORMATION_PANEL)) {
                informationPanelPlaceholder.getChildren().add(personInformationPanel.getRoot());
            } else if (event.getPanelRequestEvent().equals(HOME_PANEL)) {
                informationPanelPlaceholder.getChildren().add(homePanel.getRoot());
            } else if (event.getPanelRequestEvent().equals(HELP_PANEL)) {
                informationPanelPlaceholder.getChildren().add(helpPanel.getRoot());
            } else if (event.getPanelRequestEvent().equals(BIRTHDAY_STATISTICS_PANEL)) {
                informationPanelPlaceholder.getChildren().add(birthdayStatisticsPanel.getRoot());
            } else if (event.getPanelRequestEvent().equals(TAG_STATISTICS_PANEL)) {
                informationPanelPlaceholder.getChildren().add(tagStatisticsPanel.getRoot());
            }
        }

        currentInformationPanel = event.getPanelRequestEvent();
    }

    /**
     * Changes the stylesheet used by UI when change theme command is executed.
     */
    public void changeTheme() {
        String brightThemePath = MainApp.class.getResource(FXML_FILE_FOLDER + "BrightTheme.css").toString();
        String darkThemePath = MainApp.class.getResource(FXML_FILE_FOLDER + "DarkTheme.css").toString();
        String extensionsPath = MainApp.class.getResource(FXML_FILE_FOLDER + "Extensions.css").toString();

        String brightThemeAllPaths = "[" + extensionsPath + ", " + brightThemePath + "]";

        if (getRoot().getStylesheets().toString().equals(brightThemeAllPaths)) {
            getRoot().getStylesheets().remove(brightThemePath);
            getRoot().getStylesheets().add(darkThemePath);
        } else {
            getRoot().getStylesheets().remove(darkThemePath);
            getRoot().getStylesheets().add(brightThemePath);
        }
    }
    // @@author

    // @@author pwenzhe
    /**
     * Initializes the theme on startup to the user preferred theme.
     * @param theme
     */
    public void initTheme(String theme) {
        String initThemePath = MainApp.class.getResource(FXML_FILE_FOLDER + theme + "Theme.css").toString();

        getRoot().getStylesheets().add(initThemePath);
    }
    // @@author

    public void hide() {
        primaryStage.hide();
    }

    private void setTitle(String appTitle) {
        primaryStage.setTitle(appTitle);
    }

    /**
     * Sets the given image as the icon of the main window.
     * @param iconSource e.g. {@code "/images/help_icon.png"}
     */
    private void setIcon(String iconSource) {
        FxViewUtil.setStageIcon(primaryStage, iconSource);
    }

    /**
     * Sets the default size based on user preferences.
     */
    private void setWindowDefaultSize(UserPrefs prefs) {
        primaryStage.setHeight(prefs.getGuiSettings().getWindowHeight());
        primaryStage.setWidth(prefs.getGuiSettings().getWindowWidth());
        if (prefs.getGuiSettings().getWindowCoordinates() != null) {
            primaryStage.setX(prefs.getGuiSettings().getWindowCoordinates().getX());
            primaryStage.setY(prefs.getGuiSettings().getWindowCoordinates().getY());
        }
    }

    private void setWindowMinSize() {
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
    }

    /**
     * Returns the current size and the position of the main Window.
     */
    public GuiSettings getCurrentGuiSetting() {
        return new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
    }

    /**
     * Returns an unmodifiable child of the information panel currently displayed.
     */
    public String getCurrentInformationPanel() {
        return informationPanelPlaceholder.getChildrenUnmodifiable().toString();
    }

    /**
     * Returns the current stylesheets.
     */
    public String getCurrentStyleSheets() {
        return getRoot().getStylesheets().toString();
    }

    // @@author johnweikangong
    /**
     * Opens the home panel.
     */
    @FXML
    public void handleHome() {
        changeInformationPanel(new ChangeInformationPanelRequestEvent(HOME_PANEL));
    }

    //@@author Valerieyue
    /**
     * Opens the birthday statistics panel.
     */
    @FXML
    public void handleBirthdayStatistics() {
        changeInformationPanel(new ChangeInformationPanelRequestEvent(BIRTHDAY_STATISTICS_PANEL));
    }

    /**
     * Opens the tag statistics panel.
     */
    @FXML
    public void handleTagStatistics() {
        changeInformationPanel(new ChangeInformationPanelRequestEvent(TAG_STATISTICS_PANEL));
    }
    //@@author Valerieyue

    /**
     * Opens the help panel.
     */
    @FXML
    public void handleHelp() {
        changeInformationPanel(new ChangeInformationPanelRequestEvent(HELP_PANEL));
    }
    // @@author

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    public void handleExit() {
        raise(new ExitAppRequestEvent());
    }

    // @@author johnweikangong
    void releaseResources() {
        personInformationPanel.releaseResources();
    }
    // @@author
}
