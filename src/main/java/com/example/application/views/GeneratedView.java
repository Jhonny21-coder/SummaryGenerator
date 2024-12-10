package com.example.application.views;

import com.example.application.data.DataRecord;
import com.example.application.service.DataRecordService;
import com.example.application.service.WebsiteInfoService;
import com.example.application.service.UserService;
import com.example.application.data.History;
import com.example.application.data.AppUser;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;

import jakarta.annotation.security.PermitAll;

import java.util.concurrent.atomic.AtomicInteger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@PermitAll
@Route("generated")
public class GeneratedView extends AppLayout implements HasUrlParameter<String> {
    private final WebsiteInfoService websiteInfoService;
    private final UserService userService;
    private final DataRecordService dataRecordService;

    public GeneratedView(WebsiteInfoService websiteInfoService, UserService userService, DataRecordService dataRecordService) {
    	this.websiteInfoService = websiteInfoService;
    	this.userService = userService;
    	this.dataRecordService = dataRecordService;
    }

    @Override
    public void setParameter(BeforeEvent event, String title) {
    	createHeader();
        createMainLayout(title);
    }

    private void createMainLayout(String title) {
    	AppUser user = userService.findCurrentUser();

    	VerticalLayout mainLayout = new VerticalLayout();
    	mainLayout.addClassName("generated-main-layout");

    	Span successfulText = new Span(title + "'s content summarized successfully.");
    	successfulText.addClassName("successful-text");

    	String dateTime = LocalDateTime.now(ZoneId.of("Asia/Manila")).format(DateTimeFormatter.ofPattern("MM',' dd',' yyyy hh:mm a"));
    	Span timeText = new Span(new Icon("vaadin", "clock"), new Span("At " + dateTime));
    	timeText.addClassName("time-text");

    	Span downloadInfoText = new Span(
    	   "You can view and read the summarized content by downloading the generated PDF file below. " +
    	   "The PDF file contains the summarized content made by our AI."
    	);
    	downloadInfoText.addClassName("download-info-text");

    	Span downloadNowText = new Span("Download now");

    	SvgIcon downloadPdfIcon = new SvgIcon(new StreamResource("pdf.svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/pdf.svg")));

    	Span generatedPdf = new Span(downloadPdfIcon, new Span("summarized-content.pdf"));

    	Button downloadButton = new Button("Download", new Icon("vaadin", "download"));
    	downloadButton.addClickListener(event -> {
    	    downloadButton.setText("Downloading...");
    	    downloadButton.setIcon(new Icon("vaadin", "circle"));
    	    simulateDelay();
    	});

    	Div downloadDiv = new Div(downloadNowText, generatedPdf, downloadButton);
    	downloadDiv.addClassName("download-div");

    	mainLayout.add(successfulText, timeText, downloadInfoText, downloadDiv, createRating(user, title));

    	setContent(mainLayout);
    }

    public void simulateDelay() {
        // Vaadin's access to ensure navigation occurs on the UI thread after the delay
        UI ui = UI.getCurrent();
        ui.access(() -> {
            // Start the delay using a Timer to avoid blocking the UI thread
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    ui.access(() -> {
                        UI.getCurrent().navigate(MainView.class); // Safe navigation
                        Notification.show("PDF downloaded successfully", 3000, Notification.Position.TOP_CENTER)
                		.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    });
                }
            }, 5000); // Delay of 5 seconds
        });
    }

    private VerticalLayout createRating(AppUser sender, String title) {
    	Div starsLayout = new Div();
    	starsLayout.addClassName("stars-layout");

    	// Track the rating count
    	AtomicInteger selectedRating = new AtomicInteger(0);

    	// Generate 5 stars
    	for (int i = 1; i <= 5; i++) {
            Icon icon = new Icon("vaadin", "star-o");
            int starRating = i; // Store the star value for this icon

            // Click listener for the star
            icon.addClickListener(event -> {
            	// Update the UI and rating count
            	selectedRating.set(starRating);
            	updateStarColors(starsLayout, starRating);
            });

            starsLayout.add(icon);
    	}

    	// Instruction text
    	Span rateText = new Span("Rate this summarization");

    	// Submit button
    	Button submitButton = new Button("Submit Rating", event -> {
            int ratingCount = selectedRating.get();
            if (ratingCount > 0) {
            	dataRecordService.sendRating(sender, ratingCount, title);
            	Notification.show("Rating submitted: " + ratingCount + " stars", 3000, Notification.Position.MIDDLE);
            } else {
            	Notification.show("Please select a rating before submitting", 3000, Notification.Position.MIDDLE);
            }
    	});

    	// Layout
    	VerticalLayout ratingLayout = new VerticalLayout(rateText, starsLayout, submitButton);
    	ratingLayout.addClassName("rating-layout");

    	return ratingLayout;
    }

    // Helper method to update star colors
    private void updateStarColors(Div starsLayout, int starRating) {
    	for (int i = 0; i < starsLayout.getComponentCount(); i++) {
            Icon starIcon = (Icon) starsLayout.getComponentAt(i);
            if (i < starRating) {
            	starIcon.getElement().setAttribute("icon", "vaadin:star");
            	starIcon.getStyle().set("color", "#0ef");
            } else {
            	starIcon.getElement().setAttribute("icon", "vaadin:star-o");
            	starIcon.getStyle().set("color", "white");
            }
    	}
    }

    /*private VerticalLayout createRating(AppUser sender) {
    	Div starsLayout = new Div();
	starsLayout.addClassName("stars-layout");

    	String[] starIcons = {"star-o", "star-o", "star-o", "star-o", "star-o"};

    	for (String starIcon : starIcons) {
    	    Icon icon = new Icon("vaadin", starIcon);
    	    icon.addClickListener(event -> {
    	    	icon.getElement().setAttribute("icon", "vaadin:star");
    	    	icon.getStyle().set("color", "#0ef");
    	    });

    	    starsLayout.add(icon);
    	}

	Span rateText = new Span("Rate this summarization");

    	VerticalLayout ratingLayout = new VerticalLayout(rateText, starsLayout);
    	ratingLayout.addClassName("rating-layout");

    	return ratingLayout;
    }*/

    private void createHeader() {
    	Icon backIcon = new Icon("lumo", "arrow-left");
    	backIcon.addClickListener(event -> UI.getCurrent().navigate(MainView.class));

    	SvgIcon aiIcon = new SvgIcon(new StreamResource("ai.svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/ai.svg")));
        aiIcon.addClassName("ai-generated-icon");

        HorizontalLayout headerLayout = new HorizontalLayout(backIcon, new Span("Summarized content"), aiIcon);
        headerLayout.addClassName("generated-header");

        addToNavbar(headerLayout);
    }
}
