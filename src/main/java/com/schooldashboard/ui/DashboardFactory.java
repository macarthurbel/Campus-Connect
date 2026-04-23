package com.schooldashboard.ui;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.animation.PauseTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

public final class DashboardFactory {

    private static final String ADMIN_ROLE = "Admin Dashboard";
    private static final String TEACHER_ROLE = "Teacher Dashboard";
    private static final String STUDENT_ROLE = "Student Dashboard";

    private DashboardFactory() {
    }

    public static Scene createDashboardScene(String roleTitle, Runnable onBack) {
        DashboardState state = new DashboardState(roleTitle);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("dashboard-root");

        root.setTop(buildTopBar(state, onBack));
        root.setLeft(buildSidebar(state));
        root.setCenter(state.pageContainer);

        activateModule(state, state.allowedModules.get(0));

        return new Scene(root, 1280, 800);
    }

    private static HBox buildTopBar(DashboardState state, Runnable onBack) {
        Label title = new Label(state.roleTitle);
        title.getStyleClass().add("dashboard-title");

        Label subtitle = new Label("Academic Year 2025/2026");
        subtitle.getStyleClass().add("dashboard-subtitle");

        VBox left = new VBox(2, title, subtitle);

        Label notification = new Label("1");
        notification.getStyleClass().add("notification-badge");
        Tooltip.install(notification, new Tooltip("1 nouvelle notification"));

        Label user = new Label(state.userDisplayName());
        user.getStyleClass().add("user-pill");

        Button backButton = new Button("Back to Launcher");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(event -> onBack.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(12, left, spacer, notification, user, backButton);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        return topBar;
    }

    private static VBox buildSidebar(DashboardState state) {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(14, 10, 14, 10));

        Label appName = new Label("CampusConnect");
        appName.getStyleClass().add("sidebar-title");

        Label scope = new Label(roleScopeLabel(state.roleTitle));
        scope.getStyleClass().add("sidebar-scope");

        sidebar.getChildren().addAll(appName, scope);

        for (Module module : state.allowedModules) {
            Button item = new Button(module.label);
            item.getStyleClass().add("menu-item");
            item.setMaxWidth(Double.MAX_VALUE);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setOnAction(event -> activateModule(state, module));
            VBox.setVgrow(item, Priority.NEVER);
            state.navButtons.put(module, item);
            sidebar.getChildren().add(item);
        }

        Region push = new Region();
        VBox.setVgrow(push, Priority.ALWAYS);
        Label footer = new Label("Interactive mode");
        footer.getStyleClass().add("sidebar-footer");

        sidebar.getChildren().addAll(push, footer);
        return sidebar;
    }

    private static String roleScopeLabel(String roleTitle) {
        return switch (roleTitle) {
            case ADMIN_ROLE -> "Administrateur";
            case TEACHER_ROLE -> "Enseignant";
            case STUDENT_ROLE -> "Etudiant";
            default -> "Utilisateur";
        };
    }

    private static void activateModule(DashboardState state, Module module) {
        Node page = state.pageCache.computeIfAbsent(module, current -> buildPageForModule(state, current));
        state.pageContainer.getChildren().setAll(page);
        state.activeModule = module;
        for (Map.Entry<Module, Button> entry : state.navButtons.entrySet()) {
            entry.getValue().getStyleClass().remove("menu-item-active");
            if (entry.getKey() == module) {
                entry.getValue().getStyleClass().add("menu-item-active");
            }
        }
    }

    private static Node buildPageForModule(DashboardState state, Module module) {
        return switch (module) {
            case HOME -> buildHomePage(state);
            case STUDENTS -> buildStudentsPage(state);
            case TEACHERS -> buildTeachersPage(state);
            case COURSES -> buildCoursesPage(state);
            case GROUPS -> buildGroupsPage(state);
            case ROOMS -> buildRoomsPage(state);
            case ENROLLMENTS -> buildEnrollmentsPage(state);
            case NOTES -> buildNotesPage(state);
            case REPORT_CARDS -> buildReportCardPage(state);
            case PLANNING -> buildPlanningPage(state);
        };
    }

    private static Node buildHomePage(DashboardState state) {
        VBox page = createPageShell("Tableau de bord", "Vue globale de vos activites");

        HBox statsRow = new HBox(12,
            createStatCard("Etudiants", Integer.toString(state.students.size()), "Base active"),
            createStatCard("Enseignants", Integer.toString(state.teachers.size()), "Profils verifies"),
            createStatCard("Cours", Integer.toString(state.courses.size()), "Offre pedagogique"),
            createStatCard("Salles", Integer.toString(state.rooms.size()), "Ressources locales")
        );

        Button addStudent = new Button("+ Ajouter etudiant");
        addStudent.getStyleClass().addAll("action-button", "primary-action");
        addStudent.setOnAction(event -> runMockAction(addStudent, "Ajout...", () -> {
            Student newStudent = generateStudent(state.studentCounter.incrementAndGet());
            state.students.add(newStudent);
            state.notes.add(new NoteLine(newStudent.fullName(), "Reseaux", 14.0, 2.0));
            refreshCachedTables(state);
        }));

        Button createSession = new Button("Creer seance");
        createSession.getStyleClass().addAll("action-button", "secondary-action");
        createSession.setOnAction(event -> runMockAction(createSession, "Creation...", () -> {
            SessionLine session = new SessionLine("Vendredi", "14:00", "16:00", "Info 214 TD1", "Amphi 102", "NGAFFO");
            state.sessions.add(session);
            refreshCachedTables(state);
        }));

        Button newCourse = new Button("Nouveau cours");
        newCourse.getStyleClass().addAll("action-button", "secondary-action");
        newCourse.setOnAction(event -> runMockAction(newCourse, "Creation...", () -> {
            Course course = new Course("info " + (210 + state.courses.size()), "Nouveau module", "30h", "A valider");
            state.courses.add(course);
            refreshCachedTables(state);
        }));

        HBox actions = new HBox(8, addStudent, createSession, newCourse);

        VBox recentEnrollments = createPanel("Inscriptions recentes");
        ListView<String> recentList = new ListView<>(state.recentEnrollments);
        recentList.getStyleClass().add("simple-list");
        recentEnrollments.getChildren().add(recentList);

        VBox nextSessions = createPanel("Prochaines seances");
        ListView<String> sessionsList = new ListView<>(state.nextSessions);
        sessionsList.getStyleClass().add("simple-list");
        nextSessions.getChildren().add(sessionsList);

        HBox bottom = new HBox(12, recentEnrollments, nextSessions);
        HBox.setHgrow(recentEnrollments, Priority.ALWAYS);
        HBox.setHgrow(nextSessions, Priority.ALWAYS);

        page.getChildren().addAll(statsRow, actions, bottom);
        return page;
    }

    private static Node buildStudentsPage(DashboardState state) {
        TableView<Student> table = new TableView<>();
        table.getStyleClass().add("data-table");
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<Student, String> matriculeCol = textColumn("MATRICULE", Student::matriculeProperty, 140);
        TableColumn<Student, String> nomCol = textColumn("NOM", Student::lastNameProperty, 140);
        TableColumn<Student, String> prenomCol = textColumn("PRENOM", Student::firstNameProperty, 160);
        TableColumn<Student, String> anneeCol = textColumn("ANNEE", Student::yearProperty, 90);
        TableColumn<Student, String> filiereCol = textColumn("FILIERE", Student::trackProperty, 170);
        TableColumn<Student, Student> actionCol = actionColumn("ACTIONS", table, state.students, current -> {
            Optional<Student> edited = showStudentForm(current);
            edited.ifPresent(value -> {
                int index = state.students.indexOf(current);
                if (index >= 0) {
                    state.students.set(index, value);
                }
            });
        });

        table.getColumns().setAll(matriculeCol, nomCol, prenomCol, anneeCol, filiereCol, actionCol);
        return buildDataPage(
            "Gestion des etudiants",
            "Rechercher par matricule, nom...",
            "+ Ajouter un etudiant",
            state.students,
            table,
            studentMatches(),
            () -> showStudentForm(null).ifPresent(state.students::add)
        );
    }

    private static Node buildTeachersPage(DashboardState state) {
        TableView<Teacher> table = new TableView<>();
        table.getStyleClass().add("data-table");

        TableColumn<Teacher, String> nomCol = textColumn("NOM", Teacher::lastNameProperty, 150);
        TableColumn<Teacher, String> prenomCol = textColumn("PRENOM", Teacher::firstNameProperty, 140);
        TableColumn<Teacher, String> emailCol = textColumn("EMAIL", Teacher::emailProperty, 260);
        TableColumn<Teacher, String> statutCol = textColumn("STATUT", Teacher::statusProperty, 120);
        TableColumn<Teacher, String> deptCol = textColumn("DEPARTEMENT", Teacher::departmentProperty, 160);
        TableColumn<Teacher, Teacher> actionCol = actionColumn("ACTIONS", table, state.teachers, current -> {
            Optional<Teacher> edited = showTeacherForm(current);
            edited.ifPresent(value -> {
                int index = state.teachers.indexOf(current);
                if (index >= 0) {
                    state.teachers.set(index, value);
                }
            });
        });

        table.getColumns().setAll(nomCol, prenomCol, emailCol, statutCol, deptCol, actionCol);

        return buildDataPage(
            "Gestion des enseignants",
            "Rechercher par nom, email...",
            "+ Ajouter un enseignant",
            state.teachers,
            table,
            teacherMatches(),
            () -> showTeacherForm(null).ifPresent(state.teachers::add)
        );
    }

    private static Node buildCoursesPage(DashboardState state) {
        TableView<Course> table = new TableView<>();
        table.getStyleClass().add("data-table");

        TableColumn<Course, String> codeCol = textColumn("CODE", Course::codeProperty, 130);
        TableColumn<Course, String> titleCol = textColumn("INTITULE", Course::titleProperty, 200);
        TableColumn<Course, String> volumeCol = textColumn("VOLUME HORAIRE", Course::volumeProperty, 140);
        TableColumn<Course, String> teacherCol = textColumn("ENSEIGNANT RESPONSABLE", Course::teacherProperty, 220);
        TableColumn<Course, Course> actionCol = actionColumn("ACTIONS", table, state.courses, current -> {
            Optional<Course> edited = showCourseForm(current);
            edited.ifPresent(value -> {
                int index = state.courses.indexOf(current);
                if (index >= 0) {
                    state.courses.set(index, value);
                }
            });
        });
        table.getColumns().setAll(codeCol, titleCol, volumeCol, teacherCol, actionCol);

        return buildDataPage(
            "Gestion des cours",
            "Rechercher par code, intitule...",
            "+ Nouveau cours",
            state.courses,
            table,
            courseMatches(),
            () -> showCourseForm(null).ifPresent(state.courses::add)
        );
    }

    private static Node buildGroupsPage(DashboardState state) {
        TableView<GroupLine> table = new TableView<>();
        table.getStyleClass().add("data-table");

        TableColumn<GroupLine, String> groupCol = textColumn("GROUPE", GroupLine::nameProperty, 180);
        TableColumn<GroupLine, String> levelCol = textColumn("NIVEAU", GroupLine::levelProperty, 120);
        TableColumn<GroupLine, String> trackCol = textColumn("FILIERE", GroupLine::trackProperty, 220);
        TableColumn<GroupLine, String> countCol = textColumn("EFFECTIF", GroupLine::sizeProperty, 110);
        TableColumn<GroupLine, GroupLine> actionCol = actionColumn("ACTIONS", table, state.groups, current -> {
            Optional<GroupLine> edited = showGroupForm(current);
            edited.ifPresent(value -> {
                int index = state.groups.indexOf(current);
                if (index >= 0) {
                    state.groups.set(index, value);
                }
            });
        });

        table.getColumns().setAll(groupCol, levelCol, trackCol, countCol, actionCol);

        return buildDataPage(
            "Gestion des groupes",
            "Rechercher un groupe...",
            "+ Nouveau groupe",
            state.groups,
            table,
            groupMatches(),
            () -> showGroupForm(null).ifPresent(state.groups::add)
        );
    }

    private static Node buildRoomsPage(DashboardState state) {
        TableView<Room> table = new TableView<>();
        table.getStyleClass().add("data-table");

        TableColumn<Room, String> idCol = textColumn("IDENTIFIANT", Room::nameProperty, 180);
        TableColumn<Room, String> typeCol = textColumn("TYPE", Room::typeProperty, 170);
        TableColumn<Room, String> capacityCol = textColumn("CAPACITE", Room::capacityProperty, 140);
        TableColumn<Room, Room> actionCol = actionColumn("ACTIONS", table, state.rooms, current -> {
            Optional<Room> edited = showRoomForm(current);
            edited.ifPresent(value -> {
                int index = state.rooms.indexOf(current);
                if (index >= 0) {
                    state.rooms.set(index, value);
                }
            });
        });

        table.getColumns().setAll(idCol, typeCol, capacityCol, actionCol);

        return buildDataPage(
            "Gestion des salles",
            "Rechercher une salle...",
            "+ Nouvelle salle",
            state.rooms,
            table,
            roomMatches(),
            () -> showRoomForm(null).ifPresent(state.rooms::add)
        );
    }

    private static Node buildEnrollmentsPage(DashboardState state) {
        VBox page = createPageShell("Inscription", "Flux interactif en 3 etapes");

        Label stepLabel = new Label();
        stepLabel.getStyleClass().add("step-caption");

        HBox stepper = new HBox(10);
        stepper.setAlignment(Pos.CENTER);

        Label step1 = stepCircle("1");
        Label step2 = stepCircle("2");
        Label step3 = stepCircle("3");
        stepper.getChildren().addAll(step1, connector(), step2, connector(), step3);

        StackPane content = new StackPane();
        content.getStyleClass().add("panel-card");
        content.setPadding(new Insets(16));

        ListView<Student> studentList = new ListView<>(state.students);
        studentList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.matricule() + " - " + item.fullName() + " - " + item.track());
            }
        });
        studentList.setPrefHeight(220);

        ComboBox<Course> courseCombo = new ComboBox<>(state.courses);
        courseCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.code() + " - " + item.title());
            }
        });
        courseCombo.setButtonCell(courseCombo.getCellFactory().call(null));

        ComboBox<GroupLine> groupCombo = new ComboBox<>(state.groups);
        groupCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(GroupLine item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name() + " - " + item.track());
            }
        });
        groupCombo.setButtonCell(groupCombo.getCellFactory().call(null));

        Label confirmSummary = new Label();
        confirmSummary.setWrapText(true);

        VBox stepOneBox = new VBox(10, new Label("Etape 1: Selectionner un etudiant"), studentList);
        VBox stepTwoBox = new VBox(10, new Label("Etape 2: Selectionner un cours et un groupe"), courseCombo, groupCombo);
        VBox stepThreeBox = new VBox(10, new Label("Etape 3: Confirmation"), confirmSummary);

        Button back = new Button("Retour");
        back.getStyleClass().addAll("action-button", "secondary-action");

        Button next = new Button("Suivant");
        next.getStyleClass().addAll("action-button", "primary-action");

        HBox controls = new HBox(8, back, next);

        AtomicInteger step = new AtomicInteger(1);

        Runnable renderStep = () -> {
            int current = step.get();
            stepLabel.setText("Etape " + current + " sur 3");

            step1.getStyleClass().setAll("step-circle");
            step2.getStyleClass().setAll("step-circle");
            step3.getStyleClass().setAll("step-circle");
            if (current >= 1) {
                step1.getStyleClass().add("step-active");
            }
            if (current >= 2) {
                step2.getStyleClass().add("step-active");
            }
            if (current >= 3) {
                step3.getStyleClass().add("step-active");
            }

            if (current == 1) {
                content.getChildren().setAll(stepOneBox);
                back.setDisable(true);
                next.setText("Suivant");
            } else if (current == 2) {
                content.getChildren().setAll(stepTwoBox);
                back.setDisable(false);
                next.setText("Suivant");
            } else {
                Student selectedStudent = studentList.getSelectionModel().getSelectedItem();
                Course selectedCourse = courseCombo.getValue();
                GroupLine selectedGroup = groupCombo.getValue();
                String studentText = selectedStudent == null ? "Aucun etudiant" : selectedStudent.fullName();
                String courseText = selectedCourse == null ? "Aucun cours" : selectedCourse.title();
                String groupText = selectedGroup == null ? "Aucun groupe" : selectedGroup.name();
                confirmSummary.setText("Etudiant: " + studentText + "\nCours: " + courseText + "\nGroupe: " + groupText);
                content.getChildren().setAll(stepThreeBox);
                back.setDisable(false);
                next.setText("Confirmer inscription");
            }
        };

        back.setOnAction(event -> {
            if (step.get() > 1) {
                step.decrementAndGet();
                renderStep.run();
            }
        });

        next.setOnAction(event -> {
            if (step.get() == 1 && studentList.getSelectionModel().getSelectedItem() == null) {
                showInfo("Selection requise", "Veuillez selectionner un etudiant.");
                return;
            }
            if (step.get() == 2 && (courseCombo.getValue() == null || groupCombo.getValue() == null)) {
                showInfo("Informations manquantes", "Veuillez selectionner un cours et un groupe.");
                return;
            }
            if (step.get() < 3) {
                step.incrementAndGet();
                renderStep.run();
                return;
            }

            Student selectedStudent = studentList.getSelectionModel().getSelectedItem();
            Course selectedCourse = courseCombo.getValue();
            GroupLine selectedGroup = groupCombo.getValue();
            String newEnrollment = selectedStudent.fullName() + " - " + selectedCourse.title() + " - " + selectedGroup.name();
            state.recentEnrollments.add(0, newEnrollment);
            if (state.recentEnrollments.size() > 10) {
                state.recentEnrollments.remove(state.recentEnrollments.size() - 1);
            }
            showInfo("Inscription enregistree", "L'inscription a ete confirmee avec succes.");
            step.set(1);
            renderStep.run();
        });

        renderStep.run();
        page.getChildren().addAll(stepper, stepLabel, content, controls);
        return page;
    }

    private static Node buildNotesPage(DashboardState state) {
        VBox page = createPageShell("Notes", "Saisie et suivi des notes par etudiant");

        ComboBox<Course> courseCombo = new ComboBox<>(state.courses);
        courseCombo.setPromptText("Selectionner un cours");
        courseCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.title());
            }
        });
        courseCombo.setButtonCell(courseCombo.getCellFactory().call(null));

        TableView<NoteLine> table = new TableView<>(state.notes);
        table.getStyleClass().add("data-table");

        TableColumn<NoteLine, String> studentCol = textColumn("ETUDIANT", NoteLine::studentProperty, 260);
        TableColumn<NoteLine, String> courseCol = textColumn("COURS", NoteLine::courseProperty, 180);
        TableColumn<NoteLine, Number> noteCol = new TableColumn<>("NOTE");
        noteCol.setCellValueFactory(data -> data.getValue().scoreProperty());
        noteCol.setPrefWidth(120);

        TableColumn<NoteLine, Number> coefCol = new TableColumn<>("COEFFICIENT");
        coefCol.setCellValueFactory(data -> data.getValue().coefProperty());
        coefCol.setPrefWidth(120);

        TableColumn<NoteLine, NoteLine> actionCol = new TableColumn<>("ACTIONS");
        actionCol.setPrefWidth(220);
        actionCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button plus = new Button("+1");
            private final Button minus = new Button("-1");
            private final HBox box = new HBox(6, plus, minus);

            {
                plus.getStyleClass().add("mini-action");
                minus.getStyleClass().add("mini-danger");
                plus.setOnAction(event -> {
                    NoteLine line = getItem();
                    if (line != null) {
                        line.scoreProperty().set(Math.min(20.0, line.scoreProperty().get() + 1.0));
                        table.refresh();
                    }
                });
                minus.setOnAction(event -> {
                    NoteLine line = getItem();
                    if (line != null) {
                        line.scoreProperty().set(Math.max(0.0, line.scoreProperty().get() - 1.0));
                        table.refresh();
                    }
                });
            }

            @Override
            protected void updateItem(NoteLine item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : box);
            }
        });

        table.getColumns().setAll(studentCol, courseCol, noteCol, coefCol, actionCol);

        Button addLine = new Button("+ Ajouter note");
        addLine.getStyleClass().addAll("action-button", "primary-action");
        addLine.setOnAction(event -> runMockAction(addLine, "Ajout...", () -> {
            Student student = state.students.get(0);
            Course course = courseCombo.getValue() == null ? state.courses.get(0) : courseCombo.getValue();
            state.notes.add(new NoteLine(student.fullName(), course.title(), 12.0, 2.0));
        }));

        page.getChildren().addAll(courseCombo, addLine, table);
        return page;
    }

    private static Node buildReportCardPage(DashboardState state) {
        VBox page = createPageShell("Releve de notes", "Apercu detaille du bulletin" );

        ComboBox<Student> studentCombo = new ComboBox<>(state.students);
        studentCombo.setPromptText("Selectionner un etudiant...");
        studentCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.matricule() + " - " + item.fullName());
            }
        });
        studentCombo.setButtonCell(studentCombo.getCellFactory().call(null));

        Label profile = new Label("Selectionnez un etudiant pour afficher le releve.");
        profile.getStyleClass().add("student-profile");

        TableView<NoteLine> table = new TableView<>();
        table.getStyleClass().add("data-table");
        table.setPrefHeight(220);

        TableColumn<NoteLine, String> courseCol = textColumn("COURS", NoteLine::courseProperty, 220);
        TableColumn<NoteLine, Number> noteCol = new TableColumn<>("NOTES");
        noteCol.setCellValueFactory(data -> data.getValue().scoreProperty());
        noteCol.setPrefWidth(140);

        TableColumn<NoteLine, Number> coefCol = new TableColumn<>("COEFFICIENTS");
        coefCol.setCellValueFactory(data -> data.getValue().coefProperty());
        coefCol.setPrefWidth(140);

        TableColumn<NoteLine, String> avgCol = new TableColumn<>("MOYENNE");
        avgCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().scoreProperty().get())));
        avgCol.setPrefWidth(140);

        table.getColumns().setAll(courseCol, noteCol, coefCol, avgCol);

        Label overall = new Label("Moyenne generale: --");
        overall.getStyleClass().add("overall-average");

        Button printButton = new Button("Imprimer / PDF");
        printButton.getStyleClass().addAll("action-button", "secondary-action");
        printButton.setOnAction(event -> showInfo("Export", "Generation du PDF lancee."));

        studentCombo.setOnAction(event -> {
            Student selected = studentCombo.getValue();
            if (selected == null) {
                return;
            }
            profile.setText(selected.fullName() + " | " + selected.matricule() + " | " + selected.track() + " " + selected.year());
            ObservableList<NoteLine> lines = FXCollections.observableArrayList();
            for (NoteLine line : state.notes) {
                if (line.studentProperty().get().equals(selected.fullName())) {
                    lines.add(line);
                }
            }
            if (lines.isEmpty()) {
                lines.add(new NoteLine(selected.fullName(), "Aucune note", 0.0, 1.0));
            }
            table.setItems(lines);

            double weighted = 0;
            double weight = 0;
            for (NoteLine line : lines) {
                weighted += line.scoreProperty().get() * line.coefProperty().get();
                weight += line.coefProperty().get();
            }
            double avg = weight == 0 ? 0 : weighted / weight;
            overall.setText("Moyenne generale: " + String.format("%.2f", avg));
        });

        HBox tools = new HBox(8, printButton, overall);
        page.getChildren().addAll(studentCombo, profile, table, tools);
        return page;
    }

    private static Node buildPlanningPage(DashboardState state) {
        VBox page = createPageShell("Planning / Emploi du temps", "Semaine active et seances planifiees");

        ComboBox<String> filter = new ComboBox<>(FXCollections.observableArrayList("Tout afficher", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"));
        filter.setValue("Tout afficher");

        Button addSession = new Button("+ Nouvelle seance");
        addSession.getStyleClass().addAll("action-button", "primary-action");

        HBox topRow = new HBox(8, filter, addSession);

        TableView<SessionLine> table = new TableView<>(state.sessions);
        table.getStyleClass().add("data-table");

        TableColumn<SessionLine, String> dayCol = textColumn("JOUR", SessionLine::dayProperty, 120);
        TableColumn<SessionLine, String> startCol = textColumn("DEBUT", SessionLine::startProperty, 90);
        TableColumn<SessionLine, String> endCol = textColumn("FIN", SessionLine::endProperty, 90);
        TableColumn<SessionLine, String> courseCol = textColumn("SEANCE", SessionLine::courseProperty, 200);
        TableColumn<SessionLine, String> roomCol = textColumn("SALLE", SessionLine::roomProperty, 140);
        TableColumn<SessionLine, String> teacherCol = textColumn("ENSEIGNANT", SessionLine::teacherProperty, 180);

        table.getColumns().setAll(dayCol, startCol, endCol, courseCol, roomCol, teacherCol);

        FilteredList<SessionLine> filtered = new FilteredList<>(state.sessions, line -> true);
        table.setItems(filtered);

        filter.setOnAction(event -> {
            String choice = filter.getValue();
            filtered.setPredicate(line -> "Tout afficher".equals(choice) || line.dayProperty().get().equalsIgnoreCase(choice));
        });

        addSession.setOnAction(event -> runMockAction(addSession, "Ajout...", () -> {
            state.sessions.add(new SessionLine("Jeudi", "10:00", "12:00", "Algo avancee", "Amphi 101", "TALLA"));
        }));

        page.getChildren().addAll(topRow, table);
        return page;
    }

    private static <T> VBox buildDataPage(
        String title,
        String searchPrompt,
        String actionLabel,
        ObservableList<T> source,
        TableView<T> table,
        QueryMatcher<T> matcher,
        Runnable onAdd
    ) {
        VBox page = createPageShell(title, "Donnees completes et interactives");

        TextField search = new TextField();
        search.setPromptText(searchPrompt);
        search.getStyleClass().add("search-field");
        search.setPrefWidth(340);

        Button addButton = new Button(actionLabel);
        addButton.getStyleClass().addAll("action-button", "primary-action");
        addButton.setOnAction(event -> runMockAction(addButton, "Ouverture...", onAdd));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox controls = new HBox(8, search, spacer, addButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        FilteredList<T> filtered = new FilteredList<>(source, item -> true);
        table.setItems(filtered);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        search.textProperty().addListener((obs, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase();
            if (query.isEmpty()) {
                filtered.setPredicate(item -> true);
            } else {
                filtered.setPredicate(item -> matcher.matches(item, query));
            }
        });

        page.getChildren().addAll(controls, table);
        return page;
    }

    private static VBox createPageShell(String title, String subtitle) {
        Label pageTitle = new Label(title);
        pageTitle.getStyleClass().add("section-title");

        Label sub = new Label(subtitle);
        sub.getStyleClass().add("section-subtitle");

        VBox shell = new VBox(12, pageTitle, sub);
        shell.setPadding(new Insets(18));
        shell.setFillWidth(true);
        return shell;
    }

    private static VBox createPanel(String title) {
        Label heading = new Label(title);
        heading.getStyleClass().add("panel-title");

        VBox panel = new VBox(8, heading);
        panel.getStyleClass().add("panel-card");
        panel.setPadding(new Insets(14));
        return panel;
    }

    private static VBox createStatCard(String title, String value, String note) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("card-value");

        Label noteLabel = new Label(note);
        noteLabel.getStyleClass().add("card-note");

        VBox card = new VBox(6, titleLabel, valueLabel, noteLabel);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(14));
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private static Label stepCircle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("step-circle");
        return label;
    }

    private static Text connector() {
        Text connector = new Text("-----");
        connector.getStyleClass().add("step-connector");
        return connector;
    }

    private static <T> TableColumn<T, String> textColumn(String title, PropertyAccessor<T> accessor, double width) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> accessor.property(data.getValue()));
        column.setPrefWidth(width);
        return column;
    }

    private static <T> TableColumn<T, T> actionColumn(
        String title,
        TableView<T> table,
        ObservableList<T> source,
        EditHandler<T> onEdit
    ) {
        TableColumn<T, T> actionCol = new TableColumn<>(title);
        actionCol.setPrefWidth(160);
        actionCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button edit = new Button("Edit");
            private final Button delete = new Button("Delete");
            private final HBox wrapper = new HBox(6, edit, delete);

            {
                edit.getStyleClass().add("mini-action");
                delete.getStyleClass().add("mini-danger");
                wrapper.setAlignment(Pos.CENTER_LEFT);

                edit.setOnAction(event -> {
                    T item = getItem();
                    if (item != null) {
                        onEdit.edit(item);
                        table.refresh();
                    }
                });

                delete.setOnAction(event -> {
                    T item = getItem();
                    if (item != null) {
                        if (confirmDelete()) {
                            source.remove(item);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : wrapper);
            }
        });
        return actionCol;
    }

    private static boolean confirmDelete() {
        Dialog<ButtonType> dialog = new Dialog<>();
        applyDialogTheme(dialog);
        dialog.setTitle("Suppression");
        dialog.getDialogPane().getStyleClass().add("message-dialog");

        ButtonType cancelType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType deleteType = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(cancelType, deleteType);

        Label icon = new Label("!");
        icon.getStyleClass().addAll("message-icon", "message-warning");

        Label title = new Label("Confirmer la suppression");
        title.getStyleClass().add("message-title");

        Label body = new Label("Cet element sera retire definitivement de la liste.");
        body.getStyleClass().add("message-body");
        body.setWrapText(true);

        VBox content = new VBox(8, icon, title, body);
        content.getStyleClass().add("message-box");
        dialog.getDialogPane().setContent(content);

        Node cancelButton = dialog.getDialogPane().lookupButton(cancelType);
        Node deleteButton = dialog.getDialogPane().lookupButton(deleteType);
        cancelButton.getStyleClass().add("dialog-cancel-button");
        deleteButton.getStyleClass().add("dialog-danger-button");

        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == deleteType;
    }

    private static Optional<Student> showStudentForm(Student current) {
        Dialog<Student> dialog = createEntityDialog(
            current == null ? "Ajouter etudiant" : "Modifier etudiant",
            "Completer les informations du profil etudiant."
        );

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        styleDialogButtons(dialog, saveType);

        TextField matricule = new TextField(current == null ? "" : current.matricule());
        matricule.setPromptText("Ex: 23900125");
        TextField nom = new TextField(current == null ? "" : current.lastName());
        nom.setPromptText("Nom");
        TextField prenom = new TextField(current == null ? "" : current.firstName());
        prenom.setPromptText("Prenom");
        TextField annee = new TextField(current == null ? "" : current.year());
        annee.setPromptText("L1, L2, L3, M1...");
        TextField filiere = new TextField(current == null ? "" : current.track());
        filiere.setPromptText("Genie Informatique");

        GridPane grid = formGrid();
        addFormRow(grid, 0, "Matricule", matricule);
        addFormRow(grid, 1, "Nom", nom);
        addFormRow(grid, 2, "Prenom", prenom);
        addFormRow(grid, 3, "Annee", annee);
        addFormRow(grid, 4, "Filiere", filiere);
        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        saveButton.disableProperty().bind(
            matricule.textProperty().isEmpty()
                .or(nom.textProperty().isEmpty())
                .or(prenom.textProperty().isEmpty())
                .or(annee.textProperty().isEmpty())
                .or(filiere.textProperty().isEmpty())
        );

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                return new Student(matricule.getText().trim(), nom.getText().trim(), prenom.getText().trim(), annee.getText().trim(), filiere.getText().trim());
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private static Optional<Teacher> showTeacherForm(Teacher current) {
        Dialog<Teacher> dialog = createEntityDialog(
            current == null ? "Ajouter enseignant" : "Modifier enseignant",
            "Renseigner les informations academiques de l'enseignant."
        );

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        styleDialogButtons(dialog, saveType);

        TextField nom = new TextField(current == null ? "" : current.lastName());
        nom.setPromptText("Nom");
        TextField prenom = new TextField(current == null ? "" : current.firstName());
        prenom.setPromptText("Prenom");
        TextField email = new TextField(current == null ? "" : current.email());
        email.setPromptText("prenom.nom@enspd.cm");
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("Permanent", "Vacataire"));
        status.setValue(current == null ? "Permanent" : current.status());
        TextField departement = new TextField(current == null ? "" : current.department());
        departement.setPromptText("Informatique");

        GridPane grid = formGrid();
        addFormRow(grid, 0, "Nom", nom);
        addFormRow(grid, 1, "Prenom", prenom);
        addFormRow(grid, 2, "Email", email);
        addFormRow(grid, 3, "Statut", status);
        addFormRow(grid, 4, "Departement", departement);
        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        saveButton.disableProperty().bind(
            nom.textProperty().isEmpty()
                .or(prenom.textProperty().isEmpty())
                .or(email.textProperty().isEmpty())
                .or(departement.textProperty().isEmpty())
        );

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                return new Teacher(nom.getText().trim(), prenom.getText().trim(), email.getText().trim(), status.getValue(), departement.getText().trim());
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private static Optional<Course> showCourseForm(Course current) {
        Dialog<Course> dialog = createEntityDialog(
            current == null ? "Ajouter cours" : "Modifier cours",
            "Definir le cours, le volume horaire et l'enseignant responsable."
        );

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        styleDialogButtons(dialog, saveType);

        TextField code = new TextField(current == null ? "" : current.code());
        code.setPromptText("info 214");
        TextField intitule = new TextField(current == null ? "" : current.title());
        intitule.setPromptText("Intitule du cours");
        TextField volume = new TextField(current == null ? "" : current.volume());
        volume.setPromptText("25h");
        TextField enseignant = new TextField(current == null ? "" : current.teacher());
        enseignant.setPromptText("Nom enseignant");

        GridPane grid = formGrid();
        addFormRow(grid, 0, "Code", code);
        addFormRow(grid, 1, "Intitule", intitule);
        addFormRow(grid, 2, "Volume", volume);
        addFormRow(grid, 3, "Enseignant", enseignant);
        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        saveButton.disableProperty().bind(
            code.textProperty().isEmpty()
                .or(intitule.textProperty().isEmpty())
                .or(volume.textProperty().isEmpty())
                .or(enseignant.textProperty().isEmpty())
        );

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                return new Course(code.getText().trim(), intitule.getText().trim(), volume.getText().trim(), enseignant.getText().trim());
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private static Optional<GroupLine> showGroupForm(GroupLine current) {
        Dialog<GroupLine> dialog = createEntityDialog(
            current == null ? "Ajouter groupe" : "Modifier groupe",
            "Mettre a jour le niveau, la filiere et l'effectif du groupe."
        );

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        styleDialogButtons(dialog, saveType);

        TextField name = new TextField(current == null ? "" : current.name());
        name.setPromptText("TD1");
        TextField level = new TextField(current == null ? "" : current.level());
        level.setPromptText("L3");
        TextField track = new TextField(current == null ? "" : current.track());
        track.setPromptText("Genie Informatique");
        TextField size = new TextField(current == null ? "" : current.size());
        size.setPromptText("35");

        GridPane grid = formGrid();
        addFormRow(grid, 0, "Groupe", name);
        addFormRow(grid, 1, "Niveau", level);
        addFormRow(grid, 2, "Filiere", track);
        addFormRow(grid, 3, "Effectif", size);
        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        saveButton.disableProperty().bind(
            name.textProperty().isEmpty()
                .or(level.textProperty().isEmpty())
                .or(track.textProperty().isEmpty())
                .or(size.textProperty().isEmpty())
        );

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                return new GroupLine(name.getText().trim(), level.getText().trim(), track.getText().trim(), size.getText().trim());
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private static Optional<Room> showRoomForm(Room current) {
        Dialog<Room> dialog = createEntityDialog(
            current == null ? "Ajouter salle" : "Modifier salle",
            "Preciser la salle, son type et sa capacite."
        );

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        styleDialogButtons(dialog, saveType);

        TextField id = new TextField(current == null ? "" : current.name());
        id.setPromptText("Amphi 102");
        TextField type = new TextField(current == null ? "" : current.type());
        type.setPromptText("Amphitheatre");
        TextField capacity = new TextField(current == null ? "" : current.capacity());
        capacity.setPromptText("102 places");

        GridPane grid = formGrid();
        addFormRow(grid, 0, "Identifiant", id);
        addFormRow(grid, 1, "Type", type);
        addFormRow(grid, 2, "Capacite", capacity);
        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        saveButton.disableProperty().bind(
            id.textProperty().isEmpty()
                .or(type.textProperty().isEmpty())
                .or(capacity.textProperty().isEmpty())
        );

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                return new Room(id.getText().trim(), type.getText().trim(), capacity.getText().trim());
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private static GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("entity-form-grid");
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(8, 8, 8, 8));
        return grid;
    }

    private static void addFormRow(GridPane grid, int row, String label, Node input) {
        Label caption = new Label(label);
        caption.getStyleClass().add("form-label");
        grid.add(caption, 0, row);
        input.getStyleClass().add("form-input");
        grid.add(input, 1, row);
        if (input instanceof Region region) {
            region.setPrefWidth(340);
        }
    }

    private static QueryMatcher<Student> studentMatches() {
        return (student, q) -> student.matricule().toLowerCase().contains(q)
            || student.firstName().toLowerCase().contains(q)
            || student.lastName().toLowerCase().contains(q)
            || student.track().toLowerCase().contains(q);
    }

    private static QueryMatcher<Teacher> teacherMatches() {
        return (teacher, q) -> teacher.firstName().toLowerCase().contains(q)
            || teacher.lastName().toLowerCase().contains(q)
            || teacher.email().toLowerCase().contains(q)
            || teacher.department().toLowerCase().contains(q);
    }

    private static QueryMatcher<Course> courseMatches() {
        return (course, q) -> course.code().toLowerCase().contains(q)
            || course.title().toLowerCase().contains(q)
            || course.teacher().toLowerCase().contains(q);
    }

    private static QueryMatcher<GroupLine> groupMatches() {
        return (group, q) -> group.name().toLowerCase().contains(q)
            || group.level().toLowerCase().contains(q)
            || group.track().toLowerCase().contains(q);
    }

    private static QueryMatcher<Room> roomMatches() {
        return (room, q) -> room.name().toLowerCase().contains(q)
            || room.type().toLowerCase().contains(q)
            || room.capacity().toLowerCase().contains(q);
    }

    private static Student generateStudent(int index) {
        return new Student("2390" + (100 + index), "Nouveau", "Etudiant " + index, "L3", "Genie Informatique");
    }

    private static void refreshCachedTables(DashboardState state) {
        if (state.activeModule != null) {
            activateModule(state, state.activeModule);
        }
    }

    private static void showInfo(String title, String content) {
        Dialog<ButtonType> dialog = new Dialog<>();
        applyDialogTheme(dialog);
        dialog.setTitle(title);
        dialog.getDialogPane().getStyleClass().add("message-dialog");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        Label icon = new Label("i");
        icon.getStyleClass().addAll("message-icon", "message-info");

        Label heading = new Label(title);
        heading.getStyleClass().add("message-title");

        Label body = new Label(content);
        body.getStyleClass().add("message-body");
        body.setWrapText(true);

        VBox contentBox = new VBox(8, icon, heading, body);
        contentBox.getStyleClass().add("message-box");
        dialog.getDialogPane().setContent(contentBox);

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.getStyleClass().add("dialog-primary-button");
        dialog.showAndWait();
    }

    private static void runMockAction(Button button, String loadingLabel, Runnable action) {
        String initialText = button.getText();
        button.setDisable(true);
        button.setText(loadingLabel);

        PauseTransition pause = new PauseTransition(Duration.millis(220));
        pause.setOnFinished(event -> {
            action.run();
            button.setText(initialText);
            button.setDisable(false);
        });
        pause.playFromStart();
    }

    private static <T> Dialog<T> createEntityDialog(String title, String subtitle) {
        Dialog<T> dialog = new Dialog<>();
        applyDialogTheme(dialog);
        dialog.setTitle(title);
        dialog.getDialogPane().getStyleClass().add("entity-dialog");

        Label heading = new Label(title);
        heading.getStyleClass().add("dialog-header-title");

        Label note = new Label(subtitle + " Tous les champs peuvent etre modifies.");
        note.getStyleClass().add("dialog-header-subtitle");
        note.setWrapText(true);

        VBox header = new VBox(4, heading, note);
        header.getStyleClass().add("dialog-header-box");
        dialog.getDialogPane().setHeader(header);
        dialog.getDialogPane().setPrefWidth(560);
        return dialog;
    }

    private static void styleDialogButtons(Dialog<?> dialog, ButtonType saveType) {
        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        Node cancelButton = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (saveButton != null) {
            saveButton.getStyleClass().add("dialog-primary-button");
        }
        if (cancelButton != null) {
            cancelButton.getStyleClass().add("dialog-cancel-button");
        }
    }

    private static void applyDialogTheme(Dialog<?> dialog) {
        String stylePath = DashboardFactory.class.getResource("/styles/dashboard.css").toExternalForm();
        dialog.getDialogPane().getStylesheets().setAll(stylePath);
    }

    private enum Module {
        HOME("Tableau de bord"),
        STUDENTS("Etudiants"),
        TEACHERS("Enseignants"),
        COURSES("Cours"),
        GROUPS("Groupes"),
        ROOMS("Salles"),
        ENROLLMENTS("Inscriptions"),
        NOTES("Notes"),
        REPORT_CARDS("Releve de notes"),
        PLANNING("Planning");

        private final String label;

        Module(String label) {
            this.label = label;
        }
    }

    @FunctionalInterface
    private interface PropertyAccessor<T> {
        SimpleStringProperty property(T value);
    }

    @FunctionalInterface
    private interface QueryMatcher<T> {
        boolean matches(T value, String query);
    }

    @FunctionalInterface
    private interface EditHandler<T> {
        void edit(T value);
    }

    private record Student(String matricule, String lastName, String firstName, String year, String track) {
        String fullName() {
            return firstName + " " + lastName;
        }

        SimpleStringProperty matriculeProperty() {
            return new SimpleStringProperty(matricule);
        }

        SimpleStringProperty lastNameProperty() {
            return new SimpleStringProperty(lastName);
        }

        SimpleStringProperty firstNameProperty() {
            return new SimpleStringProperty(firstName);
        }

        SimpleStringProperty yearProperty() {
            return new SimpleStringProperty(year);
        }

        SimpleStringProperty trackProperty() {
            return new SimpleStringProperty(track);
        }
    }

    private record Teacher(String lastName, String firstName, String email, String status, String department) {
        SimpleStringProperty lastNameProperty() {
            return new SimpleStringProperty(lastName);
        }

        SimpleStringProperty firstNameProperty() {
            return new SimpleStringProperty(firstName);
        }

        SimpleStringProperty emailProperty() {
            return new SimpleStringProperty(email);
        }

        SimpleStringProperty statusProperty() {
            return new SimpleStringProperty(status);
        }

        SimpleStringProperty departmentProperty() {
            return new SimpleStringProperty(department);
        }
    }

    private record Course(String code, String title, String volume, String teacher) {
        SimpleStringProperty codeProperty() {
            return new SimpleStringProperty(code);
        }

        SimpleStringProperty titleProperty() {
            return new SimpleStringProperty(title);
        }

        SimpleStringProperty volumeProperty() {
            return new SimpleStringProperty(volume);
        }

        SimpleStringProperty teacherProperty() {
            return new SimpleStringProperty(teacher);
        }
    }

    private record GroupLine(String name, String level, String track, String size) {
        SimpleStringProperty nameProperty() {
            return new SimpleStringProperty(name);
        }

        SimpleStringProperty levelProperty() {
            return new SimpleStringProperty(level);
        }

        SimpleStringProperty trackProperty() {
            return new SimpleStringProperty(track);
        }

        SimpleStringProperty sizeProperty() {
            return new SimpleStringProperty(size);
        }
    }

    private record Room(String name, String type, String capacity) {
        SimpleStringProperty nameProperty() {
            return new SimpleStringProperty(name);
        }

        SimpleStringProperty typeProperty() {
            return new SimpleStringProperty(type);
        }

        SimpleStringProperty capacityProperty() {
            return new SimpleStringProperty(capacity);
        }
    }

    private static final class NoteLine {
        private final SimpleStringProperty student;
        private final SimpleStringProperty course;
        private final DoubleProperty score;
        private final DoubleProperty coef;

        private NoteLine(String student, String course, double score, double coef) {
            this.student = new SimpleStringProperty(student);
            this.course = new SimpleStringProperty(course);
            this.score = new SimpleDoubleProperty(score);
            this.coef = new SimpleDoubleProperty(coef);
        }

        SimpleStringProperty studentProperty() {
            return student;
        }

        SimpleStringProperty courseProperty() {
            return course;
        }

        DoubleProperty scoreProperty() {
            return score;
        }

        DoubleProperty coefProperty() {
            return coef;
        }
    }

    private record SessionLine(String day, String start, String end, String course, String room, String teacher) {
        SimpleStringProperty dayProperty() {
            return new SimpleStringProperty(day);
        }

        SimpleStringProperty startProperty() {
            return new SimpleStringProperty(start);
        }

        SimpleStringProperty endProperty() {
            return new SimpleStringProperty(end);
        }

        SimpleStringProperty courseProperty() {
            return new SimpleStringProperty(course);
        }

        SimpleStringProperty roomProperty() {
            return new SimpleStringProperty(room);
        }

        SimpleStringProperty teacherProperty() {
            return new SimpleStringProperty(teacher);
        }
    }

    private static final class DashboardState {
        private final String roleTitle;
        private final ObservableList<Module> allowedModules;
        private final StackPane pageContainer;
        private final Map<Module, Node> pageCache;
        private final Map<Module, Button> navButtons;

        private final ObservableList<Student> students;
        private final ObservableList<Teacher> teachers;
        private final ObservableList<Course> courses;
        private final ObservableList<GroupLine> groups;
        private final ObservableList<Room> rooms;
        private final ObservableList<NoteLine> notes;
        private final ObservableList<SessionLine> sessions;
        private final ObservableList<String> recentEnrollments;
        private final ObservableList<String> nextSessions;

        private final AtomicInteger studentCounter;
        private Module activeModule;

        private DashboardState(String roleTitle) {
            this.roleTitle = roleTitle;
            this.allowedModules = modulesForRole(roleTitle);
            this.pageContainer = new StackPane();
            this.pageContainer.getStyleClass().add("page-container");
            this.pageCache = new EnumMap<>(Module.class);
            this.navButtons = new EnumMap<>(Module.class);

            this.students = FXCollections.observableArrayList(
                new Student("23900030", "BALLA", "Jordan Mateo", "L3", "Genie Informatique"),
                new Student("23900060", "EDAMBO", "Mac Arthur", "L3", "Genie Informatique"),
                new Student("23600142", "FONSI ROMELD", "Tresor", "L3", "Genie Informatique"),
                new Student("23600178", "JEUKING", "Martial Bienvenu", "L3", "Genie Informatique"),
                new Student("22000405", "TCHINDA", "Sophie", "M1", "Genie Civil")
            );

            this.teachers = FXCollections.observableArrayList(
                new Teacher("NGAFFO", "Armelle", "armelle.ngaffo@enspd.cm", "Permanent", "Informatique"),
                new Teacher("TALLA", "Emmanuel", "emmanuel.talla@enspd.cm", "Permanent", "Informatique"),
                new Teacher("NDJOCK", "Blaise", "blaise.ndjock@enspd.cm", "Vacataire", "Mathematiques"),
                new Teacher("MBIDA", "Sylvie", "sylvie.mbida@enspd.cm", "Permanent", "Genie Civil")
            );

            this.courses = FXCollections.observableArrayList(
                new Course("info 214", "Merise", "25h", "Blaise NDJOCK"),
                new Course("info 218", "Systeme d'exploitation", "40h", "Emmanuel TALLA"),
                new Course("info 302", "IA appliquee", "30h", "Armelle NGAFFO")
            );

            this.groups = FXCollections.observableArrayList(
                new GroupLine("TD1", "L3", "Genie Informatique", "35"),
                new GroupLine("TD2", "L3", "Genie Informatique", "31"),
                new GroupLine("TP1", "M1", "Genie Civil", "20")
            );

            this.rooms = FXCollections.observableArrayList(
                new Room("10BP1", "Salle TP", "40 places"),
                new Room("Amphi 101", "Amphitheatre", "100 places"),
                new Room("17BS1", "Salle Cours", "180 places")
            );

            this.notes = FXCollections.observableArrayList(
                new NoteLine("Jordan Mateo BALLA", "Merise", 15.0, 2.0),
                new NoteLine("Jordan Mateo BALLA", "Systeme d'exploitation", 13.0, 2.0),
                new NoteLine("Mac Arthur EDAMBO", "Merise", 14.0, 2.0),
                new NoteLine("Martial Bienvenu JEUKING", "IA appliquee", 16.0, 3.0)
            );

            this.sessions = FXCollections.observableArrayList(
                new SessionLine("Lundi", "08:00", "10:00", "Merise TD1", "Amphi 102", "TALLA"),
                new SessionLine("Mardi", "10:00", "12:00", "Info 214 TD1", "Amphi 102", "NGAFFO"),
                new SessionLine("Mercredi", "06:00", "07:00", "Info 214 TD1", "Amphi 102", "NDJOCK")
            );

            this.recentEnrollments = FXCollections.observableArrayList(
                "Mac Arthur EDAMBO - Merise - TD1",
                "Jordan Mateo BALLA - Merise - TD1"
            );

            this.nextSessions = FXCollections.observableArrayList(
                "Info 214 TD1 - 20:25 a 10:20",
                "Info 214 TD1 - 10:00 a 11:00",
                "Info 214 TD1 - 06:00 a 07:00"
            );

            this.studentCounter = new AtomicInteger(students.size() + 1);
        }

        private String userDisplayName() {
            return switch (roleTitle) {
                case ADMIN_ROLE -> "Admin ENSPD";
                case TEACHER_ROLE -> "Enseignant ENSPD";
                case STUDENT_ROLE -> "Etudiant ENSPD";
                default -> "User";
            };
        }

        private static ObservableList<Module> modulesForRole(String roleTitle) {
            List<Module> modules = switch (roleTitle) {
                case ADMIN_ROLE -> List.of(
                    Module.HOME,
                    Module.STUDENTS,
                    Module.TEACHERS,
                    Module.COURSES,
                    Module.GROUPS,
                    Module.ROOMS,
                    Module.ENROLLMENTS,
                    Module.NOTES,
                    Module.REPORT_CARDS,
                    Module.PLANNING
                );
                case TEACHER_ROLE -> List.of(
                    Module.HOME,
                    Module.COURSES,
                    Module.NOTES,
                    Module.REPORT_CARDS,
                    Module.PLANNING
                );
                case STUDENT_ROLE -> List.of(
                    Module.HOME,
                    Module.COURSES,
                    Module.REPORT_CARDS,
                    Module.PLANNING
                );
                default -> List.of(Module.HOME);
            };
            return FXCollections.observableArrayList(new ArrayList<>(modules));
        }
    }
}
