package com.example.application.views;

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

import java.util.List;

@PermitAll
@Route("history")
public class HistoryView extends AppLayout {

    private final UserService userService;

    public HistoryView(UserService userService) {
       	this.userService = userService;

       	createHeader();
       	createMainLayout();
    }

    private void createMainLayout() {
    	AppUser user = userService.findCurrentUser();

	VerticalLayout mainLayout = new VerticalLayout();
	mainLayout.addClassName("history-main-layout");

    	List<History> histories = userService.getUserHistories(user.getId());

    	for (int i = 0; i < histories.size(); i++) {
    	    History history = histories.get(i);

    	    HorizontalLayout historyLayout = new HorizontalLayout();
    	    historyLayout.addClassName("history-layout");
    	    historyLayout.addClickListener(event -> UI.getCurrent().navigate(GeneratedView.class, history.getTitle()));

    	    Span title = new Span(truncate(history.getTitle(), 18));
    	    Span dateTime = new Span(history.getDateTime());

	    Icon deleteIcon = new Icon("vaadin", "trash");
	    deleteIcon.addClickListener(event -> {
	    	userService.removeUserHistory(history);
	    	mainLayout.remove(historyLayout);
	    });

    	    historyLayout.add(dateTime, title, deleteIcon);
    	    mainLayout.add(historyLayout);

    	    if (i % 2 == 0) {
    	    	historyLayout.addClassName("even-layout");
    	    } else {
    	    	historyLayout.addClassName("odd-layout");
    	    }
    	}

    	setContent(mainLayout);
    }

    private String truncate(String text, int length) {
    	return text.length() > length ? text.substring(0, length) + "..." : text;
    }

    private void createHeader() {
        Icon backIcon = new Icon("lumo", "arrow-left");
        backIcon.addClickListener(event -> UI.getCurrent().navigate(MainView.class));

        SvgIcon aiIcon = new SvgIcon(new StreamResource("ai.svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/ai.svg")));
        aiIcon.addClassName("ai-generated-icon");

        HorizontalLayout headerLayout = new HorizontalLayout(backIcon, new Span("History"), aiIcon);
        headerLayout.addClassName("generated-header");

        addToNavbar(headerLayout);
    }
}
