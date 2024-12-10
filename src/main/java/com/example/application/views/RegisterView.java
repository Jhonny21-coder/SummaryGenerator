package com.example.application.views;

import com.example.application.data.AppUser;
import com.example.application.service.UserService;
import com.example.application.config.CloudinaryService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

@AnonymousAllowed
@Route("register")
@PageTitle("Register | TAG")
public class RegisterView extends AppLayout {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    private final TextField firstNameField = new TextField("First Name");
    private final TextField lastNameField = new TextField("Last Name");
    private final EmailField emailField = new EmailField("Email");
    private final PasswordField passwordField = new PasswordField("Password");
    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Upload upload = new Upload(buffer);
    private final String OTHER_INFORMATION = "Next";
    private final Button registerButton = new Button("Register");

    // Create a Binder
    private final Binder<AppUser> binder = new Binder<>(AppUser.class);

    private final String USER_FOLDER = "user_images";

    public RegisterView(UserService userService, CloudinaryService cloudinaryService) {
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
	addClassName("register-form");

	classNames();

	firstNameField.setRequiredIndicatorVisible(true);
	firstNameField.setErrorMessage("Last name is required");

	lastNameField.setRequiredIndicatorVisible(true);
        lastNameField.setErrorMessage("First name is required");

        emailField.setErrorMessage("Enter a valid email address");
	emailField.setRequiredIndicatorVisible(true);

	passwordField.setErrorMessage("Password is required");
        passwordField.setRequiredIndicatorVisible(true);

        // Bind fields and set required error messages
	binder.forField(firstNameField).asRequired("First name is required").bind(AppUser::getFirstName, AppUser::setFirstName);
	binder.forField(lastNameField).asRequired("Last name is required").bind(AppUser::getLastName, AppUser::setLastName);
	binder.forField(emailField).asRequired("Email is required").bind(AppUser::getEmail, AppUser::setEmail);
	binder.forField(passwordField).asRequired("Password is required").bind(AppUser::getPassword, AppUser::setPassword);

	int maxFileSizeInBytes = 10 * 1024 * 1024;

	upload.setMaxFileSize(maxFileSizeInBytes);
        upload.setAcceptedFileTypes("image/png", "image/jpeg");

	//Register button functionality
	registerButton.addClassName("button1");
	registerButton.addClickListener( event -> {
	    try {
		String firstName = firstNameField.getValue();
                String lastName = lastNameField.getValue();
                String email = emailField.getValue();
                String password = passwordField.getValue();
                InputStream inputStream = buffer.getInputStream();

		if(inputStream.available() == 0) {
                   Notification.show("Please upload a profile image.", 2000, Position.TOP_STRETCH)
                     .addThemeVariants(NotificationVariant.LUMO_ERROR);
		}else{
		   // Create a temporary file to store the uploaded image
		   File tempFile = File.createTempFile("tempImage", ".png");
		   tempFile.deleteOnExit(); // Ensure the file is deleted on exit

		   // Save the uploaded image to the temporary file
		   FileOutputStream fos = new FileOutputStream(tempFile);
		   byte[] imageBytes = inputStream.readAllBytes();
		   fos.write(imageBytes);

                   // Upload the image to Cloudinary
                   String imageUrl = cloudinaryService.uploadImage(tempFile, USER_FOLDER);

                   writeDataToSqlFile("true", firstName, lastName, email, password, "ROLE_USER", imageUrl);

                   userService.registerUser(firstName, lastName, email, password, imageUrl);

        	   // Show notification
        	   Notification.show("Registration successful! Please login.", 3000, Position.TOP_CENTER)
              	     	.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		   getUI().ifPresent(ui -> ui.navigate(LoginView.class));
		}
	    } catch (IOException e) {
                    Notification.show("Error uploading artwork image", 3000, Notification.Position.TOP_CENTER);
            }
	});

	handleFileUpload();

	H1 headerText = new H1("Register");
	headerText.addClassName("login-text");

	Accordion accordion = new Accordion();
        accordion.addClassName("accordion");

        accordionLayout(accordion);

	Button loginButton = new Button("Already have an account? Login here.", e -> UI.getCurrent().navigate(LoginView.class));
	loginButton.addClassName("register-login-button");

	VerticalLayout fieldsLayout = new VerticalLayout(headerText, accordion, loginButton);
	fieldsLayout.addClassName("register-fields-layout");

        setContent(fieldsLayout);
    }

    private void writeDataToSqlFile(String enabled, String firstName, String lastName, String email, String password, String role, String profileImage) {
        String filename = "src/main/resources/data.sql";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))){
            String query = String.format("(%s, '%s', '%s', '%s', '%s', '%s', '%s');", enabled, firstName, lastName, email, password, role, profileImage);
            writer.println(query);
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
	    new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("20em", 2));
        return formLayout;
    }

    public void classNames(){
        firstNameField.addClassName("register-field");
        lastNameField.addClassName("register-field");
        emailField.addClassName("email");
        passwordField.addClassName("password");
        upload.addClassName("register-upload");
    }

    public void handleFileUpload(){
        // Create the button and set the uploadLayout as its component
        Button uploadButton = new Button("Upload image", new Icon("lumo", "photo"));

        // Set the uploadButton as the upload button
        upload.setUploadButton(uploadButton);
        upload.addFileRejectedListener(event -> {
            String jsCode = "var audio = new Audio('./images/error.wav'); audio.play();";
            UI.getCurrent().getPage().executeJs(jsCode);

            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(errorMessage, 5000,
                    Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        UploadsI18N i18n = new UploadsI18N();

        i18n.getError().setFileIsTooBig(
                "The image exceeds the maximum allowed size of 10MB.");
        i18n.getError().setIncorrectFileType(
                "The provided image does not have the correct format (PNG or JPEG).");
        upload.setI18n(i18n);
    }

    public void accordionLayout(Accordion accordion){
        FormLayout customerDetailsFormLayout = createFormLayout();
        customerDetailsFormLayout.addClassName("register-form-layout");

        FormLayout formLayout = new FormLayout();
	formLayout.setResponsiveSteps(
            // Use one column by default
            new ResponsiveStep("0", 1),
            // Use two columns, if layout's width exceeds 500px
            new ResponsiveStep("500px", 2)
        );

        // Stretch the username field over 2 columns
        formLayout.setColspan(emailField, 2);

        AccordionPanel mainPanel = accordion.add("Personal Information", formLayout);
	mainPanel.addClassName("panel");

        AccordionPanel customDetailsPanel = accordion.add(OTHER_INFORMATION, customerDetailsFormLayout);
	customDetailsPanel.addClassName("panel");

        // Continue Button
        Icon continueButton = new Icon(VaadinIcon.ARROW_CIRCLE_RIGHT_O);
        continueButton.addClassName("button2");
        continueButton.addClickListener(event -> {
            if(binder.isValid() && binder.validate().isOk()){
               customDetailsPanel.setOpened(true);
            }else if(emailField.isInvalid()){
               Notification.show("Please enter a valid email address.", 2000, Position.TOP_STRETCH)
                     .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }else if(!binder.isValid() && !binder.validate().isOk()){
               Notification.show("Please complete the registration fields.", 2000, Position.TOP_STRETCH)
                     .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Icon backIcon = new Icon(VaadinIcon.ARROW_CIRCLE_LEFT_O);
        backIcon.addClassName("back-continue");
        backIcon.addClickListener(event -> {
            mainPanel.setOpened(true);
        });

        // Adding to Form Layout
        formLayout.add(firstNameField, 2);
        formLayout.add(lastNameField, 2);
        formLayout.add(emailField, 2);
        formLayout.add(passwordField, 2);
        mainPanel.addContent(continueButton);

        Avatar avatar = new Avatar();
        avatar.addClassName("register-avatar");
        avatar.setVisible(false);

	upload.addSucceededListener(event -> {
            MemoryBuffer buffer = (MemoryBuffer) upload.getReceiver();

            try {
                InputStream inputStream = buffer.getInputStream();
                byte[] bytes = inputStream.readAllBytes();
                StreamResource resource = new StreamResource("avatar-image", () -> new ByteArrayInputStream(bytes));
                avatar.setImageResource(resource);
                avatar.setVisible(true);
            } catch (IOException e) {
                Notification.show("Error uploading artwork image", 3000, Notification.Position.TOP_CENTER);
            }
        });

        customDetailsPanel.addContent(backIcon, upload, avatar);
        customDetailsPanel.addContent(registerButton);

        // Setting icons to text field
        firstNameField.setSuffixComponent(new Icon(VaadinIcon.TEXT_LABEL));
        lastNameField.setSuffixComponent(new Icon(VaadinIcon.TEXT_LABEL));
        emailField.setSuffixComponent(new Icon(VaadinIcon.ENVELOPE_OPEN_O));
    }
}
