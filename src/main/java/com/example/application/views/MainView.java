package com.example.application.views;

import com.example.application.config.SecurityService;
import com.example.application.data.AppUser;
import com.example.application.service.UserService;
import com.example.application.config.GoogleUserSession;
import com.example.application.config.CloudinaryService;
import com.example.application.service.WebsiteInfoService;

import com.vaadin.flow.component.applayout.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.component.avatar.*;
import com.vaadin.flow.component.icon.*;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.*;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.theme.lumo.LumoUtility;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import jakarta.annotation.security.PermitAll;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;

import java.net.URL;
import java.net.HttpURLConnection;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@PermitAll
@Route("")
public class MainView extends AppLayout {
    private final UserService userService;
    private final SecurityService securityService;
    private final CloudinaryService cloudinaryService;
    private final WebsiteInfoService websiteInfoService;

    private final String USER_FOLDER = "user_images";

    public MainView(UserService userService, SecurityService securityService, CloudinaryService cloudinaryService, WebsiteInfoService websiteInfoService) {
    	this.userService = userService;
    	this.securityService = securityService;
    	this.cloudinaryService = cloudinaryService;
    	this.websiteInfoService = websiteInfoService;

    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken) {
            try {
                GoogleUserSession userSession = new GoogleUserSession();
                String imageUrl = userSession.getGoogleProfileImageUrl();

                if (imageUrl != null) {
                   // Download the image
                   InputStream inputStream = downloadImage(imageUrl);

                   // Createva temporary file automatically delete on exit
                   File tempFile = File.createTempFile("tempImage", ".png");
                   tempFile.deleteOnExit(); // Ensure the file is deleted on exit

                   // Save the uploaded image to the temporary file
                   FileOutputStream fos = new FileOutputStream(tempFile);
                   byte[] imageBytes = inputStream.readAllBytes();
                   fos.write(imageBytes);

                   OAuth2AuthenticatedPrincipal principal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();

                   String firstName = principal.getAttribute("given_name");
                   String lastName = principal.getAttribute("family_name");
                   String email = principal.getAttribute("email");
                   String profileImage = cloudinaryService.uploadImage(tempFile, USER_FOLDER);

                   AppUser user = userService.findUserByEmail(email);

                   if(user == null){
                      userService.addGoogleUser(firstName, lastName, email, profileImage);
                   }

                   VaadinSession.getCurrent().setAttribute("user", email);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            VaadinSession.getCurrent().setAttribute("user", authentication.getName());
        }

    	AppUser user = userService.findCurrentUser();

    	createHeader(user);
    	createMainLayout();
    }

    public InputStream downloadImage(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        if (connection.getResponseCode() == 200) {
            return connection.getInputStream();  // InputStream of the image
        } else {
            throw new Exception("Failed to download image from " + imageUrl);
        }
    }

    private void createMainLayout() {
    	Span headerText = new Span("Summary Generator");
    	headerText.addClassName("header-text");

    	Span text = new Span("Let AI summarize the website's content");
    	text.addClassName("text");

    	TextField textField = new TextField("URL to summarize");
    	textField.setPlaceholder("Enter URL");

    	ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);

        NativeLabel progressBarLabel = new NativeLabel("Summarizing please wait...");
        progressBarLabel.addClassName(LumoUtility.TextColor.SECONDARY);

        Div progressDiv = new Div(progressBarLabel, progressBar);
        progressDiv.addClassName("progress-div");
        progressDiv.setVisible(false);

    	Button summarizeButton = new Button("Summarize");
    	summarizeButton.addClassName("done-button");
    	summarizeButton.addClickListener(event -> {
    	    String url = textField.getValue().trim();

    	    if (url.isEmpty()) {
    		Notification.show("Please enter a valid URL", 2000, Notification.Position.MIDDLE)
            		.addThemeVariants(NotificationVariant.LUMO_ERROR);
	    } else if (!url.startsWith("https://")) {
    		Notification.show("The URL must start with 'https://'", 2000, Notification.Position.MIDDLE)
            		.addThemeVariants(NotificationVariant.LUMO_ERROR);
	    } else {
    		summarizeButton.setText("Generating...");
    		progressDiv.setVisible(true);
    		String title = websiteInfoService.getDomainName(url);
    		String dateTime = LocalDateTime.now(ZoneId.of("Asia/Manila"))
            		.format(DateTimeFormatter.ofPattern("MM',' dd',' yyyy hh:mm a"));
    		userService.addHistoryToUser(title, dateTime);
    		simulateDelay(title);
	    }
    	});

    	SvgIcon aiAvatar = new SvgIcon(new StreamResource("ai.svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/ai.svg")));
    	aiAvatar.addClassName("ai-avatar");

	HorizontalLayout textFieldLayout = new HorizontalLayout(textField, summarizeButton);
	textFieldLayout.addClassName("field-layout");

    	VerticalLayout mainLayout = new VerticalLayout(headerText, text, aiAvatar, textFieldLayout, progressDiv);
    	mainLayout.addClassName("main-layout");

    	setContent(mainLayout);
    }

    public void simulateDelay(String title) {
    	// Vaadin's access to ensure navigation occurs on the UI thread after the delay
    	UI ui = UI.getCurrent();
    	ui.access(() -> {
            // Start the delay using a Timer to avoid blocking the UI thread
            new java.util.Timer().schedule(new java.util.TimerTask() {
            	@Override
            	public void run() {
                    ui.access(() -> {
                    	UI.getCurrent().navigate(GeneratedView.class, title); // Safe navigation
                    });
            	}
            }, 5000); // Delay of 5 seconds
    	});
    }

    private HorizontalLayout createSecondLayout() {
    	HorizontalLayout secondLayout = new HorizontalLayout();
    	secondLayout.addClassName("second-layout");
    	secondLayout.add(new Div(new Icon("vaadin", "star"), new Span("RATE")));
    	return secondLayout;
    }

    private void createHeader(AppUser user) {
    	Button history = new Button("History", new Icon("vaadin", "time-backward"), e -> UI.getCurrent().navigate(HistoryView.class));
    	Span name = new Span("Welcome, " + user.getFirstName());
    	Avatar avatar = new Avatar("", user.getProfileImage());

    	HorizontalLayout headerLayout = new HorizontalLayout(history, name, avatar);
    	headerLayout.addClassName("header-layout");

	SvgIcon aiIcon = new SvgIcon(new StreamResource("ai svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/ai.svg")));

	Icon starIcon = new Icon("vaadin", "star");
	starIcon.addClassName("admin-star-icon");
	starIcon.addClickListener(event -> UI.getCurrent().navigate(RatingView.class));

	if (user.getRole().equals("ROLE_ADMIN")) {
	    starIcon.setVisible(true);
	} else {
	    starIcon.setVisible(false);
	}

    	HorizontalLayout buttonLayout = new HorizontalLayout(
    	   new Div(aiIcon, new Span("AI-driven tool")),
    	   starIcon,
    	   new Button("Sign out", new Icon("vaadin", "sign-out"), e -> securityService.logout())
    	);

    	VerticalLayout mainLayout = new VerticalLayout(buttonLayout, headerLayout);
    	mainLayout.addClassName("header-main-layout");

    	addToNavbar(mainLayout);
    }
}
