package com.example.application.views;

import com.example.application.data.DataRecord;
import com.example.application.service.DataRecordService;
import com.example.application.service.WebsiteInfoService;
import com.example.application.service.UserService;
import com.example.application.service.ViewTrackerService;
import com.example.application.data.History;
import com.example.application.data.AppUser;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.component.Component;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.Chart;
import com.github.appreciated.apexcharts.config.TitleSubtitle;
import com.github.appreciated.apexcharts.config.Tooltip;
import com.github.appreciated.apexcharts.helper.Series;
import com.github.appreciated.apexcharts.config.XAxis;
import com.github.appreciated.apexcharts.config.YAxis;
import com.github.appreciated.apexcharts.config.Grid;
import com.github.appreciated.apexcharts.config.series.*;
import com.github.appreciated.apexcharts.config.chart.*;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.subtitle.*;
import com.github.appreciated.apexcharts.config.subtitle.builder.*;
import com.github.appreciated.apexcharts.config.plotoptions.*;
import com.github.appreciated.apexcharts.config.plotoptions.builder.*;
import com.github.appreciated.apexcharts.config.xaxis.builder.*;
import com.github.appreciated.apexcharts.config.plotoptions.bar.builder.ColorsBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.bar.builder.RangesBuilder;
import com.github.appreciated.apexcharts.config.yaxis.builder.LabelsBuilder;
import com.github.appreciated.apexcharts.config.yaxis.builder.AxisTicksBuilder;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Objects;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import reactor.core.publisher.Flux;

@RolesAllowed("ADMIN")
@Route("rating")
public class RatingView extends AppLayout {

    private final DataRecordService dataRecordService;
    private final UserService userService;
    private final ViewTrackerService viewTrackerService;
    private final Flux<DataRecord> ratingFlux;

    private VerticalLayout userRatingLayout = new VerticalLayout();
    private Div parentContainer = new Div();
    private Div trafficContainer = new Div();
    private Span totalRatings = new Span();
    private VerticalLayout ratingPercentageLayout = new VerticalLayout();
    private VerticalLayout emptyRatingLayout = new VerticalLayout();
    private Span dateSpan = new Span();

    private ApexCharts chart;
    private LocalDate currentDate = LocalDate.now(ZoneId.of("Asia/Manila"));
    private int days = 0;

    public RatingView(DataRecordService dataRecordService, UserService userService, ViewTrackerService viewTrackerService, Flux<DataRecord> ratingFlux) {
    	this.dataRecordService = dataRecordService;
    	this.userService = userService;
    	this.viewTrackerService = viewTrackerService;
    	this.ratingFlux = ratingFlux;

	parentContainer.addClassName("rating-main-layout");
    	userRatingLayout.addClassName("rating-user-main-layout");
    	ratingPercentageLayout.addClassName("rating-layout");

	createHeader();
	subscribeRealtimeRatingUpdates();
    	createMainLayout();
    }

    // Method to initialize all the layouts
    private void createMainLayout() {
        AppUser user = userService.findCurrentUser();

        List<DataRecord> ratings = dataRecordService.getAllRatings();
        Collections.sort(ratings, Comparator.comparing(DataRecord::getRatingCount).reversed());

        if (ratings.isEmpty()) {
           SvgIcon ratingIcon = getSvgIcon("rating");
           Span emptyText = new Span("Rating is empty. New ratings will appear here.");
           emptyRatingLayout.add(ratingIcon, emptyText);
           emptyRatingLayout.addClassName("empty-rating-layout");
           userRatingLayout.add(emptyRatingLayout);
        }

	updateRatingPercentage();
        createUserRating(ratings);
        updateTotalRatings();
        createTrafficLayout();

        parentContainer.add(ratingPercentageLayout, totalRatings, createUserRatingHeader(ratings), userRatingLayout);

        setContent(createTabSheet());
    }

    // Method to create a TabSheet for navigation
    private TabSheet createTabSheet() {
    	TabSheet tabSheet = new TabSheet();
        tabSheet.addClassName("rating-tabsheet");
	tabSheet.add(new Span("RATINGS"), new LazyComponent(() -> parentContainer));
	tabSheet.add(new Span("TRAFFIC"), new LazyComponent(() -> trafficContainer));
	return tabSheet;
    }

    // Helper class to lazily load the component
    public class LazyComponent extends Div {
        public LazyComponent(SerializableSupplier<? extends Component> supplier) {
            addAttachListener(e -> {
                if (getElement().getChildCount() == 0) {
                    add(supplier.get());
                }
            });
        }
    }

    // Method to create a layout for traffic
    private void createTrafficLayout() {
    	ComboBox<String> timeUnits = new ComboBox<>();
    	timeUnits.setItems("Days", "Weeks", "Months", "Years");
    	timeUnits.setAllowCustomValue(false);

	Icon backwardIcon = new Icon("lumo", "angle-left");
	backwardIcon.setColor("white");

	Icon forwardIcon = new Icon("lumo", "angle-right");
	forwardIcon.setColor("var(--lumo-contrast-50pct)");

	backwardIcon.addClickListener(event -> {
	    boolean is12DaysBefore = currentDate.equals(LocalDate.now(ZoneId.of("Asia/Manila")).minusDays(11));

	    if (!is12DaysBefore) {
	    	days++;
	    	updateApexCharts();
	    	forwardIcon.setColor("white");
	    }
	});

	forwardIcon.addClickListener(event -> {
	    boolean isCurrentDate = currentDate.equals(LocalDate.now(ZoneId.of("Asia/Manila")));
	    boolean isNearCurrentDate = currentDate.equals(LocalDate.now(ZoneId.of("Asia/Manila")).minusDays(1));

	    if (!isCurrentDate) {
	    	days--;
	    	updateApexCharts();
	    }

	    if (isNearCurrentDate) {
	        forwardIcon.setColor("var(--lumo-contrast-50pct)");
	    }
	});

	LocalDate activeDate = LocalDate.now(ZoneId.of("Asia/Manila"));
	LocalDate previousDate = LocalDate.now(ZoneId.of("Asia/Manila")).minusDays(1);

	int currentViews = viewTrackerService.getViewsByDate(activeDate);
	int previousViews = viewTrackerService.getViewsByDate(previousDate);

	String viewsChange = calculateViewsChange(previousViews, currentViews);

	updateDate(activeDate);

	HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.addClassName("traffic-header");
	headerLayout.add(timeUnits, new Div(dateSpan, backwardIcon, forwardIcon));

	trafficContainer.add(headerLayout, createInteractiveTrafficStatsChart(activeDate, viewsChange));
    }

    // Update ApexCharts configuration
    private void updateApexCharts() {
	currentDate = LocalDate.now(ZoneId.of("Asia/Manila")).minusDays(days);
    	updateDate(currentDate);

	// GET THE SECOND COMPONENT IN THE PARENT CONTAINER (TRAFFICCONTAINER)
	ApexCharts currentChart = getCurrentChart();
	ApexCharts newChart = createInteractiveTrafficStatsChart(currentDate, getViewsChange());
	trafficContainer.replace(currentChart, newChart);
    }

    // Get the current ApexChart from parent container
    private ApexCharts getCurrentChart() {
    	// GET THE SECOND COMPONENT IN THE PARENT CONTAINER (TRAFFICCONTAINER)
	ApexCharts currentChart = null;
	Component firstComponent = trafficContainer.getComponentAt(1); // GET THE SECOND COMPONENT (CHART)
	if (firstComponent instanceof ApexCharts) {
	    currentChart = (ApexCharts) firstComponent; // CAST IT TO APEXCHART
	}

	return currentChart;
    }

    // Method to create a Span for date
    private void updateDate(LocalDate activeDate) {
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String formattedDate = activeDate.format(formatter);
        dateSpan.setText(formattedDate);
    }

    private String getViewsChange() {
    	LocalDate previousDate = LocalDate.now(ZoneId.of("Asia/Manila")).minusDays(days + 1);

        int currentViews = viewTrackerService.getViewsByDate(currentDate);
        int previousViews = viewTrackerService.getViewsByDate(previousDate);

        return calculateViewsChange(previousViews, currentViews);
    }

    // Method to calculate the difference and percentage change in views
    public String calculateViewsChange(int previousViews, int currentViews) {
	if (previousViews == 0 && currentViews == 0) {
	    return "0 (0%)";
	}

	// Calculate the difference in views
	int difference = currentViews - previousViews;

	// Calculate the percentage change for current views relative to previous views
	double percentageChange = 0;

	// If previous views are not zero
	if (previousViews != 0) {
	    percentageChange = ((double) difference / previousViews) * 100;
	} else if (currentViews > 0) {
	    // If previous views are 0 and current views > 0, percentage change is capped at 100%
	    percentageChange = 100;
	}

	// Cap the percentage to 100% if it's greater than 100 for increases
	if (percentageChange > 100 && difference > 0) {
	    percentageChange = 100;
	}

	// Format the difference and percentage with the appropriate signs
	String formattedDifference = (difference > 0 ? "+" : "") + difference;
	String formattedPercentage = (percentageChange > 0 ? "+" : "") + String.format("%.0f", percentageChange) + "%";

	// Return WordPress-like format
	return formattedDifference + " (" + formattedPercentage + ")";
    }

    private ApexCharts createInteractiveTrafficStatsChart(LocalDate activeDate, String viewsChange) {
    	Map<LocalDate, Integer> dailyViews = viewTrackerService.getDailyViewsForPast12Days();

	List<String> barColors = createBarColors(activeDate, dailyViews);

	int currentViews = viewTrackerService.getViewsByDate(activeDate);

    	chart = ApexChartsBuilder.get()
    		.withChart(ChartBuilder.get()
    		    .withType(Type.BAR)
    		    .withWidth("100%")
    		    .withHeight("300px")
    		    .build()
		)
    		.withTitle(TitleSubtitleBuilder.get()
    		    .withText(currentViews + " Views")
    		    .withAlign(Align.CENTER)
    		    .withStyle(StyleBuilder.get().withColor("white").withFontSize("20px").build())
    		    .build()
    		)
		.withSubtitle(TitleSubtitleBuilder.get()
                    .withText(viewsChange)
                    .withAlign(Align.CENTER)
                    .withStyle(StyleBuilder.get().withColor("white").withFontSize("15px").build())
                    .build()
		)
		.withYaxis(YAxisBuilder.get()
		    .withAxisTicks(AxisTicksBuilder.get()
		    	.withColor("#CCCCCC")
		    	.build()
		    )
		    .build()
		)
		.withXaxis(XAxisBuilder.get()
		    .withTitle(TitleBuilder.get()
		        .withText("Date Range")
		        .withStyle(com.github.appreciated.apexcharts.config.xaxis.title.builder.StyleBuilder.get().withColor("white").build())
		        .build()
		    )
		    .withLabels(com.github.appreciated.apexcharts.config.xaxis.builder.LabelsBuilder.get()
		    	.withStyle(com.github.appreciated.apexcharts.config.xaxis.labels.builder.StyleBuilder.get()
			    .withColors(Collections.nCopies(12, "#CCCCCC"))
			    .build()
		    	)
		    	.build()
		    )
		    .withCategories(createXAxisCategories())
		    .build()
		)
		.withPlotOptions(PlotOptionsBuilder.get()
		    .withBar(BarBuilder.get()
		    	.withHorizontal(false)
		    	.withColumnWidth("50%")
		    	.withColors(com.github.appreciated.apexcharts.config.plotoptions.bar.builder.ColorsBuilder.get()
		    	    .withBackgroundBarColors(barColors.stream().toArray(String[]::new))
		    	    .build()
		    	)
		    	.build()
		    )
		    .build()
		)
		.withTooltip(com.github.appreciated.apexcharts.config.builder.TooltipBuilder.get()
		    .withEnabled(false)
		    .build()
		)
		.withSeries(createDataSeriesForPast12Days())
		.build();

	chart.setWidth("100%");
	chart.setHeight("400px");

    	return chart;
    }

    private List<String> createBarColors(LocalDate today, Map<LocalDate, Integer> dailyViews) {
    	return dailyViews.entrySet().stream()
            .sorted(Map.Entry.comparingByKey()) // Ensure alignment with data
            .map(entry -> entry.getKey().isEqual(today) ? "green" : "#0000")
            .toList();
    }

    private Series<Integer> createDataSeriesForPast12Days() {
    	// Fetch data for the past 12 days
        Map<LocalDate, Integer> dailyViews = viewTrackerService.getDailyViewsForPast12Days();

    	// Collect the daily views into a Number array
    	Integer[] data = dailyViews.entrySet().stream()
            .sorted(Map.Entry.comparingByKey()) // Ensure ascending order
            //.sorted((entry1, entry2) -> entry2.getKey().compareTo(entry1.getKey())) // DESCENDING
            .map(Map.Entry::getValue) // Get the value (views)
            .toArray(Integer[]::new); // Collect as an Integer array

	return new Series<Integer>("Views", data);
    }

    private String[] createXAxisCategories() {
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    	LocalDate today = LocalDate.now(ZoneId.of("Asia/Manila"));
    	String[] categories = new String[12];

    	for (int i = 0; i < 12; i++) {
            categories[11 - i] = today.minusDays(i).format(formatter); // Descending order
    	}

    	return categories;
    }

    // Method to subscribe for realtime updates for incoming or new rating
    private void subscribeRealtimeRatingUpdates() {
    	ratingFlux.subscribe(rating -> {
	    getUI().ifPresent(ui -> ui.access(() -> {
	    	userRatingLayout.remove(emptyRatingLayout);
	    	addNewRating(rating);
	    	updateRatingPercentage();
	    	updateTotalRatings();
	    	// Scroll to the bottom
                parentContainer.getElement().executeJs("this.scrollTop = 0;");
	    }));
	});
    }

    // Method to add new rating layout
    private void addNewRating(DataRecord rating) {
    	AppUser sender = rating.getSender();

	Avatar avatar = new Avatar("", sender.getProfileImage());
	Span name = new Span(sender.getFullName());
	Span title = new Span(rating.getTitle());

	HorizontalLayout ratingLayout = new HorizontalLayout(
	    new Div(avatar, name), new Span(new Span("New"), title)
	);

	int ratingCount = rating.getRatingCount();

	Div starDiv = new Div();

	for (int i = 0; i < ratingCount; i++) {
            starDiv.add(new Icon("vaadin", "star"));
	}

	Span ratingText = new Span(String.valueOf(ratingCount + (ratingCount >= 2 ? " stars" : " star")));

	HorizontalLayout userStarLayout = new HorizontalLayout(starDiv, ratingText);
	userStarLayout.addClassName("rating-user-star-layout");

	VerticalLayout mainLayout = new VerticalLayout(ratingLayout, userStarLayout);
	mainLayout.addClassName("new-rating-layout");

	userRatingLayout.addComponentAsFirst(mainLayout);
    }

    // Method to create a seach field for rating
    private TextField createSearchRating(List<DataRecord> ratings) {
    	TextField searchField = new TextField("Search by username");
    	searchField.addClassName("search-rating-field");
    	searchField.setPlaceholder("Search...");
    	searchField.setValueChangeMode(ValueChangeMode.EAGER);
    	searchField.addValueChangeListener(event -> {
            userRatingLayout.removeAll();

            String value = event.getValue();
            if (value != null && !value.trim().isEmpty()) {
            	List<DataRecord> filteredRatings = ratings.stream()
                    .filter(rating -> rating.getSender().getFullName().toLowerCase().contains(value.toLowerCase()))
                    .collect(Collectors.toList());

               	createUserRating(filteredRatings);
            } else {
            	// If search is empty, show all ratings
            	createUserRating(ratings);
            }
    	});

    	return searchField;
    }

    // Method to create a header for individual rating
    private VerticalLayout createUserRatingHeader(List<DataRecord> ratings) {
    	VerticalLayout userRatingHeader = new VerticalLayout(
    	    new Span("Individual rating"),
    	    new Div(createFilterOptions(ratings), createSearchRating(ratings)),
    	    createRatingHeader()
    	);
    	userRatingHeader.addClassName("user-rating-header");
    	return userRatingHeader;
    }

    // Method to create an options for filtering the ratings
    private ComboBox<String> createFilterOptions(List<DataRecord> ratings) {
    	ComboBox<String> filterField = new ComboBox<>("Filter by");
    	filterField.setHelperText(ratings.size() + " ratings for all");
    	filterField.addClassName("filter-combo-box");
    	filterField.setPlaceholder("Filter...");
    	filterField.setItems("All", "1 star", "2 stars", "3 stars", "4 stars", "5 stars");
    	filterField.setValue("All");
    	filterField.setAllowCustomValue(false);

    	filterField.addValueChangeListener(event -> {
            String selectedValue = event.getValue();
            userRatingLayout.removeAll();

            // Validate selected value
	    if (selectedValue == null || selectedValue.isEmpty() ||
    		(!selectedValue.equals("All") && !selectedValue.matches("\\d+\\s+stars?"))) {
    		Notification.show("Invalid value selected. Please choose a valid filter.", 3000, Notification.Position.MIDDLE)
            		.addThemeVariants(NotificationVariant.LUMO_ERROR);
    		return;
	    }

            List<DataRecord> filteredRatings;

            if ("All".equals(selectedValue)) {
            	filteredRatings = dataRecordService.getAllRatings();
            } else {
            	int count = Integer.parseInt(selectedValue.split(" ")[0]);
            	filteredRatings = dataRecordService.getRatingsByRatingCount(count);
            }

	    filterField.setHelperText(filteredRatings.size() + " ratings for " + selectedValue.toLowerCase());
            createUserRating(filteredRatings);
    	});

    	return filterField;
    }

    // Method to create a layout for individual rating
    private void createUserRating(List<DataRecord> ratings) {
    	ratings.forEach(rating -> {
    	    AppUser sender = rating.getSender();

    	    Avatar avatar = new Avatar("", sender.getProfileImage());
    	    Span name = new Span(sender.getFullName());
    	    Span title = new Span(rating.getTitle());

    	    HorizontalLayout ratingLayout = new HorizontalLayout(
    	     	new Div(avatar, name), title
    	    );

    	    int ratingCount = rating.getRatingCount();

    	    Div starDiv = new Div();

    	    for (int i = 0; i < ratingCount; i++) {
    	    	starDiv.add(new Icon("vaadin", "star"));
    	    }

    	    Span ratingText = new Span(String.valueOf(ratingCount + (ratingCount >= 2 ? " stars" : " star")));

	    HorizontalLayout userStarLayout = new HorizontalLayout(starDiv, ratingText);
            userStarLayout.addClassName("rating-user-star-layout");

    	    VerticalLayout mainLayout = new VerticalLayout(ratingLayout, userStarLayout);
    	    mainLayout.addClassName("rating-user-layout");

    	    userRatingLayout.add(mainLayout);
    	});
    }

    // Method to create a header for individual rating
    private Div createRatingHeader() {
    	return new Div(new Span("User/Stars"), new Span("Title/Total"));
    }

    // Method to update total ratings
    private void updateTotalRatings() {
    	String overallPercentage = dataRecordService.calculateOverallRatingPercentage();
    	totalRatings.removeAll();
    	totalRatings.setText(overallPercentage);
    }

    // Method to update percentage for star ratings
    private void updateRatingPercentage() {
    	ratingPercentageLayout.removeAll();
    	ratingPercentageLayout.add(new Span("Percentage"));

    	Map<Integer, Double> ratingPercentages = dataRecordService.calculateRatingDistribution();

        for (Map.Entry<Integer, Double> entry : ratingPercentages.entrySet()) {
	    HorizontalLayout starLayout = new HorizontalLayout();
	    starLayout.addClassName("rating-star-layout");

            for (int i = 0; i < entry.getKey(); i++) {
            	Icon starIcon = new Icon("vaadin", "star");
            	starLayout.add(starIcon);
            }

            starLayout.add(new Span(String.format("%.2f %s", entry.getValue(), "%")));
            ratingPercentageLayout.add(starLayout);
        }
    }

    private SvgIcon getSvgIcon(String iconName) {
    	return new SvgIcon(new StreamResource(iconName + ".svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/" +  iconName + ".svg")));
    }

    // Method to create a header for the layout
    private void createHeader() {
        Icon backIcon = new Icon("lumo", "arrow-left");
        backIcon.addClickListener(event -> UI.getCurrent().navigate(MainView.class));

        SvgIcon aiIcon = new SvgIcon(new StreamResource("ai.svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/ai.svg")));
        aiIcon.addClassName("ai-generated-icon");

        HorizontalLayout headerLayout = new HorizontalLayout(backIcon, new Span("Rating statistics"), aiIcon);
        headerLayout.addClassName("generated-header");

        addToNavbar(headerLayout);
    }
}
