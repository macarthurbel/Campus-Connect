package com.schooldashboard.ui;

import dao.CoursDao;
import dao.EnseignantDao;
import dao.EtudiantDao;
import dao.GroupeDao;
import dao.InscriptionDao;
import dao.NotesDao;
import dao.SalleDao;
import dao.SeanceDao;
import services.AuthService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import models.StatutEnseignant;
import models.TypeGroupe;
import models.TypeSalle;
import models.UserRole;

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
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public final class DashboardFactory {

    private static final String ADMIN_ROLE = "Admin Dashboard";
    private static final String TEACHER_ROLE = "Teacher Dashboard";
    private static final String STUDENT_ROLE = "Student Dashboard";

    private DashboardFactory() {
    }

    public static Scene createDashboardScene(String roleTitle, String username, Integer linkedEntityId, Runnable onBack) {
        DashboardState state = new DashboardState(roleTitle, username, linkedEntityId);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("dashboard-root");

        root.setTop(buildTopBar(state, onBack));
        root.setLeft(buildSidebar(state));
        root.setCenter(state.pageContainer);

        activateModule(state, state.allowedModules.get(0));

        return new Scene(root, 1280, 800);
    }

    private static java.time.LocalDate parseDay(String dayName) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.DayOfWeek targetDay = switch (dayName) {
            case "Lundi" -> java.time.DayOfWeek.MONDAY;
            case "Mardi" -> java.time.DayOfWeek.TUESDAY;
            case "Mercredi" -> java.time.DayOfWeek.WEDNESDAY;
            case "Jeudi" -> java.time.DayOfWeek.THURSDAY;
            case "Vendredi" -> java.time.DayOfWeek.FRIDAY;
            case "Samedi" -> java.time.DayOfWeek.SATURDAY;
            case "Dimanche" -> java.time.DayOfWeek.SUNDAY;
            default -> today.getDayOfWeek();
        };
        
        // Find the next occurrence of the target day
        int daysUntilTarget = (targetDay.getValue() - today.getDayOfWeek().getValue() + 7) % 7;
        if (daysUntilTarget == 0) {
            // Today is the target day, use next week's occurrence
            daysUntilTarget = 7;
        }
        return today.plusDays(daysUntilTarget);
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

        HBox statsRow;
        if (state.isStudentMode()) {
            statsRow = new HBox(12,
                createStatCard("Mes cours", Integer.toString(state.courses.size()), "Cours qui vous concernent"),
                createStatCard("Mes notes", Integer.toString(state.notes.size()), "Notes personnelles"),
                createStatCard("Mes inscriptions", Integer.toString(state.recentEnrollments.size()), "Historique personnel"),
                createStatCard("Mes seances", Integer.toString(state.sessions.size()), "Planning personnel")
            );
        } else if (state.isTeacherMode()) {
            statsRow = new HBox(12,
                createStatCard("Mes cours", Integer.toString(state.courses.size()), "Cours dont vous etes responsable"),
                createStatCard("Mes etudiants", Integer.toString(state.students.size()), "Etudiants suivis"),
                createStatCard("Mes notes", Integer.toString(state.notes.size()), "Notes de vos cours"),
                createStatCard("Mes seances", Integer.toString(state.sessions.size()), "Planning enseignant")
            );
        } else {
            statsRow = new HBox(12,
                createStatCard("Etudiants", Integer.toString(state.students.size()), "Base active"),
                createStatCard("Enseignants", Integer.toString(state.teachers.size()), "Profils verifies"),
                createStatCard("Cours", Integer.toString(state.courses.size()), "Offre pedagogique"),
                createStatCard("Salles", Integer.toString(state.rooms.size()), "Ressources locales")
            );
        }

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

        VBox recentEnrollments = createPanel(state.isStudentMode() ? "Mes inscriptions recentes" : state.isTeacherMode() ? "Inscriptions de mes cours" : "Inscriptions recentes");
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

        if (state.isStudentMode() || state.isTeacherMode()) {
            Label personalScope = new Label("Vous visualisez uniquement vos informations personnelles.");
            personalScope.getStyleClass().add("section-subtitle");
            page.getChildren().addAll(statsRow, personalScope, bottom);
        } else {
            page.getChildren().addAll(statsRow, actions, bottom);
        }
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
                Optional<CredentialInput> credential = showCredentialForm(
                    "Identifiants etudiant",
                    state.authService.findLinkedIdentifier(UserRole.STUDENT, current.id()).orElse(value.matricule()),
                    "Laisser vide pour conserver le mot de passe actuel"
                );
                if (credential.isEmpty()) {
                    return;
                }

                Student updated = new Student(current.id(), value.matricule(), value.lastName(), value.firstName(), value.year(), value.track());
                models.Etudiant entity = toEtudiantEntity(updated);
                entity.setId(updated.id());
                state.etudiantDao.update(entity);
                state.authService.upsertLinkedCredentials(UserRole.STUDENT, updated.id(), credential.get().identifier(), credential.get().password());
                replaceById(state.students, updated, Student::id);
            });
        }, current -> {
            state.authService.deleteLinkedCredentials(UserRole.STUDENT, current.id());
            return state.etudiantDao.deleteById(current.id());
        });

        table.getColumns().setAll(matriculeCol, nomCol, prenomCol, anneeCol, filiereCol, actionCol);
        return buildDataPage(
            "Gestion des etudiants",
            "Rechercher par matricule, nom...",
            "+ Ajouter un etudiant",
            state.students,
            table,
            studentMatches(),
            () -> showStudentForm(null).ifPresent(value -> {
                Optional<CredentialInput> credential = showCredentialForm("Identifiants etudiant", value.matricule(), "Definir le mot de passe initial");
                if (credential.isEmpty()) {
                    return;
                }

                models.Etudiant saved = state.etudiantDao.save(toEtudiantEntity(value));
                state.authService.upsertLinkedCredentials(UserRole.STUDENT, saved.getId(), credential.get().identifier(), credential.get().password());
                state.students.add(new Student(saved.getId(), saved.getMatricule(), saved.getNom(), saved.getPrenom(), Integer.toString(saved.getAnnee()), saved.getFiliere()));
            })
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
                Optional<CredentialInput> credential = showCredentialForm(
                    "Identifiants enseignant",
                    state.authService.findLinkedIdentifier(UserRole.TEACHER, current.id()).orElse(value.email()),
                    "Laisser vide pour conserver le mot de passe actuel"
                );
                if (credential.isEmpty()) {
                    return;
                }

                Teacher updated = new Teacher(current.id(), value.lastName(), value.firstName(), value.email(), value.status(), value.department());
                models.Enseignant entity = toEnseignantEntity(updated);
                entity.setId(updated.id());
                state.enseignantDao.update(entity);
                state.authService.upsertLinkedCredentials(UserRole.TEACHER, updated.id(), credential.get().identifier(), credential.get().password());
                replaceById(state.teachers, updated, Teacher::id);
            });
        }, current -> {
            state.authService.deleteLinkedCredentials(UserRole.TEACHER, current.id());
            return state.enseignantDao.deleteById(current.id());
        });

        table.getColumns().setAll(nomCol, prenomCol, emailCol, statutCol, deptCol, actionCol);

        return buildDataPage(
            "Gestion des enseignants",
            "Rechercher par nom, email...",
            "+ Ajouter un enseignant",
            state.teachers,
            table,
            teacherMatches(),
            () -> showTeacherForm(null).ifPresent(value -> {
                Optional<CredentialInput> credential = showCredentialForm("Identifiants enseignant", value.email(), "Definir le mot de passe initial");
                if (credential.isEmpty()) {
                    return;
                }

                models.Enseignant saved = state.enseignantDao.save(toEnseignantEntity(value));
                state.authService.upsertLinkedCredentials(UserRole.TEACHER, saved.getId(), credential.get().identifier(), credential.get().password());
                state.teachers.add(new Teacher(saved.getId(), saved.getNom(), saved.getPrenom(), saved.getEmail(), toTeacherStatus(saved.getStatut()), saved.getDepartement()));
            })
        );
    }

    private static Node buildCoursesPage(DashboardState state) {
        TableView<Course> table = new TableView<>();
        table.getStyleClass().add("data-table");

        TableColumn<Course, String> codeCol = textColumn("CODE", Course::codeProperty, 130);
        TableColumn<Course, String> titleCol = textColumn("INTITULE", Course::titleProperty, 200);
        TableColumn<Course, String> volumeCol = textColumn("VOLUME HORAIRE", Course::volumeProperty, 140);
        TableColumn<Course, String> teacherCol = textColumn("ENSEIGNANT RESPONSABLE", Course::teacherProperty, 220);

        if (!state.isAdminMode()) {
            table.getColumns().setAll(codeCol, titleCol, volumeCol, teacherCol);
            return buildReadOnlyDataPage(
                state.isTeacherMode() ? "Mes cours" : "Mes cours",
                "Rechercher mes cours...",
                state.courses,
                table,
                courseMatches()
            );
        }

        TableColumn<Course, Course> actionCol = actionColumn("ACTIONS", table, state.courses, current -> {
            Optional<Course> edited = showCourseForm(current, state.teachers);
            edited.ifPresent(value -> {
                Course updated = new Course(current.id(), value.code(), value.title(), value.volume(), value.teacher());
                models.Cours entity = toCoursEntity(updated, state.teachers, state.enseignantDao);
                entity.setId(updated.id());
                state.coursDao.update(entity);
                replaceById(state.courses, updated, Course::id);
            });
        }, current -> state.coursDao.deleteById(current.id()));
        table.getColumns().setAll(codeCol, titleCol, volumeCol, teacherCol, actionCol);

        return buildDataPage(
            "Gestion des cours",
            "Rechercher par code, intitule...",
            "+ Nouveau cours",
            state.courses,
            table,
            courseMatches(),
            () -> showCourseForm(null, state.teachers).ifPresent(value -> {
                models.Cours saved = state.coursDao.save(toCoursEntity(value, state.teachers, state.enseignantDao));
                String teacher = saved.getEnseignantResponsable() == null ? "A valider" : saved.getEnseignantResponsable().getNom() + " " + saved.getEnseignantResponsable().getPrenom();
                state.courses.add(new Course(saved.getId(), saved.getCode(), saved.getIntitule(), saved.getVolumeHoraire() + "h", teacher));
            })
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
                GroupLine updated = new GroupLine(current.id(), value.name(), value.level(), value.track(), value.size());
                models.Groupe entity = toGroupeEntity(updated);
                entity.setId(updated.id());
                state.groupeDao.update(entity);
                replaceById(state.groups, updated, GroupLine::id);
            });
        }, current -> state.groupeDao.deleteById(current.id()));

        table.getColumns().setAll(groupCol, levelCol, trackCol, countCol, actionCol);

        return buildDataPage(
            "Gestion des groupes",
            "Rechercher un groupe...",
            "+ Nouveau groupe",
            state.groups,
            table,
            groupMatches(),
            () -> showGroupForm(null).ifPresent(value -> {
                models.Groupe saved = state.groupeDao.save(toGroupeEntity(value));
                state.groups.add(new GroupLine(saved.getId(), saved.getNom(), saved.getType().name(), "General", Integer.toString(saved.getCapacite())));
            })
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
                Room updated = new Room(current.id(), value.name(), value.type(), value.capacity());
                models.Salle entity = toSalleEntity(updated);
                entity.setId(updated.id());
                state.salleDao.update(entity);
                replaceById(state.rooms, updated, Room::id);
            });
        }, current -> state.salleDao.deleteById(current.id()));

        table.getColumns().setAll(idCol, typeCol, capacityCol, actionCol);

        return buildDataPage(
            "Gestion des salles",
            "Rechercher une salle...",
            "+ Nouvelle salle",
            state.rooms,
            table,
            roomMatches(),
            () -> showRoomForm(null).ifPresent(value -> {
                models.Salle saved = state.salleDao.save(toSalleEntity(value));
                state.rooms.add(new Room(saved.getId(), saved.getIdentifiantSalle(), toRoomType(saved.getTypeSalle()), saved.getCapaciteAccueil() + " places"));
            })
        );
    }

    private static Node buildEnrollmentsPage(DashboardState state) {
        if (state.isAdminMode()) {
            return buildAdminEnrollmentsPage(state);
        } else if (state.isStudentMode()) {
            return buildStudentEnrollmentsPage(state);
        } else {
            return buildTeacherEnrollmentsPage(state);
        }
    }

    private static Node buildAdminEnrollmentsPage(DashboardState state) {
        VBox page = createPageShell("Inscriptions", "Vue d'ensemble delle inscriptions par groupe");

        // Créer une liste groupée par groupe/matière
        Map<String, ObservableList<String>> enrollmentsByGroup = new LinkedHashMap<>();
        for (String enrollment : state.recentEnrollments) {
            // Format: "Etudiant - Cours - Groupe"
            String[] parts = enrollment.split(" - ");
            if (parts.length >= 3) {
                String group = parts[2];
                enrollmentsByGroup.computeIfAbsent(group, k -> FXCollections.observableArrayList()).add(enrollment);
            }
        }

        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        for (Map.Entry<String, ObservableList<String>> entry : enrollmentsByGroup.entrySet()) {
            String groupName = entry.getKey();
            ObservableList<String> enrollments = entry.getValue();

            Label groupLabel = new Label(groupName);
            groupLabel.getStyleClass().add("section-title");

            ListView<String> enrollmentList = new ListView<>(enrollments);
            enrollmentList.setPrefHeight(150);
            enrollmentList.getStyleClass().add("simple-list");

            VBox groupPanel = new VBox(8, groupLabel, enrollmentList);
            groupPanel.getStyleClass().add("panel-card");
            groupPanel.setPadding(new Insets(12));

            container.getChildren().add(groupPanel);
        }

        if (enrollmentsByGroup.isEmpty()) {
            Label empty = new Label("Aucune inscription pour le moment");
            empty.getStyleClass().add("section-subtitle");
            container.getChildren().add(empty);
        }

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        page.getChildren().add(scroll);

        return page;
    }

    private static Node buildStudentEnrollmentsPage(DashboardState state) {
        VBox page = createPageShell("Mes Inscriptions", "Mes inscriptions recentes par groupe");

        Student currentStudent = state.currentStudent();
        if (currentStudent == null) {
            Label empty = new Label("Aucune information etudiant disponible");
            empty.getStyleClass().add("section-subtitle");
            page.getChildren().add(empty);
            return page;
        }

        // Filtrer les inscriptions de l'étudiant actuel
        Map<String, ObservableList<String>> enrollmentsByGroup = new LinkedHashMap<>();
        String studentName = currentStudent.fullName();
        
        for (String enrollment : state.recentEnrollments) {
            // Format: "Etudiant - Cours - Groupe"
            if (enrollment.startsWith(studentName)) {
                String[] parts = enrollment.split(" - ");
                if (parts.length >= 3) {
                    String group = parts[2];
                    enrollmentsByGroup.computeIfAbsent(group, k -> FXCollections.observableArrayList()).add(enrollment);
                }
            }
        }

        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        if (enrollmentsByGroup.isEmpty()) {
            Label empty = new Label("Vous n'avez aucune inscription pour le moment");
            empty.getStyleClass().add("section-subtitle");
            container.getChildren().add(empty);
        } else {
            for (Map.Entry<String, ObservableList<String>> entry : enrollmentsByGroup.entrySet()) {
                String groupName = entry.getKey();
                ObservableList<String> enrollments = entry.getValue();

                Label groupLabel = new Label(groupName);
                groupLabel.getStyleClass().add("section-title");

                ListView<String> enrollmentList = new ListView<>(enrollments);
                enrollmentList.setPrefHeight(100);
                enrollmentList.getStyleClass().add("simple-list");

                VBox groupPanel = new VBox(8, groupLabel, enrollmentList);
                groupPanel.getStyleClass().add("panel-card");
                groupPanel.setPadding(new Insets(12));

                container.getChildren().add(groupPanel);
            }
        }

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        page.getChildren().add(scroll);

        return page;
    }

    private static Node buildTeacherEnrollmentsPage(DashboardState state) {
        VBox page = createPageShell("Inscriptions", "Inscriptions par groupe");

        // Afficher les inscriptions groupées par groupe
        Map<String, ObservableList<String>> enrollmentsByGroup = new LinkedHashMap<>();
        for (String enrollment : state.recentEnrollments) {
            String[] parts = enrollment.split(" - ");
            if (parts.length >= 3) {
                String group = parts[2];
                enrollmentsByGroup.computeIfAbsent(group, k -> FXCollections.observableArrayList()).add(enrollment);
            }
        }

        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        for (Map.Entry<String, ObservableList<String>> entry : enrollmentsByGroup.entrySet()) {
            String groupName = entry.getKey();
            ObservableList<String> enrollments = entry.getValue();

            Label groupLabel = new Label(groupName);
            groupLabel.getStyleClass().add("section-title");

            ListView<String> enrollmentList = new ListView<>(enrollments);
            enrollmentList.setPrefHeight(120);
            enrollmentList.getStyleClass().add("simple-list");

            VBox groupPanel = new VBox(8, groupLabel, enrollmentList);
            groupPanel.getStyleClass().add("panel-card");
            groupPanel.setPadding(new Insets(12));

            container.getChildren().add(groupPanel);
        }

        if (enrollmentsByGroup.isEmpty()) {
            Label empty = new Label("Aucune inscription pour le moment");
            empty.getStyleClass().add("section-subtitle");
            container.getChildren().add(empty);
        }

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        page.getChildren().add(scroll);

        return page;
    }

    private static Node buildNotesPage(DashboardState state) {
        VBox page = createPageShell("Notes", "Saisie et suivi des notes par etudiant");

        TableView<NoteLine> table = new TableView<>(state.notes);
        table.getStyleClass().add("data-table");

        TableColumn<NoteLine, String> studentCol = textColumn("ETUDIANT", NoteLine::studentProperty, 220);
        TableColumn<NoteLine, String> courseCol = textColumn("COURS", NoteLine::courseProperty, 180);
        TableColumn<NoteLine, Number> noteCol = new TableColumn<>("NOTE");
        noteCol.setCellValueFactory(data -> data.getValue().scoreProperty());
        noteCol.setPrefWidth(100);

        TableColumn<NoteLine, Number> coefCol = new TableColumn<>("COEF");
        coefCol.setCellValueFactory(data -> data.getValue().coefProperty());
        coefCol.setPrefWidth(80);

        if (state.isStudentMode()) {
            table.getColumns().setAll(courseCol, noteCol, coefCol);
        } else if (state.isTeacherMode()) {
            TableColumn<NoteLine, NoteLine> actionCol = new TableColumn<>("ACTIONS");
            actionCol.setPrefWidth(180);
            actionCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
            actionCol.setCellFactory(col -> new TableCell<>() {
                private final Button edit = new Button("Editer");
                private final Button delete = new Button("Supprimer");
                private final HBox box = new HBox(6, edit, delete);

                {
                    edit.getStyleClass().add("mini-action");
                    delete.getStyleClass().add("mini-danger");
                    edit.setOnAction(event -> {
                        NoteLine line = getItem();
                        if (line != null) {
                            Optional<NoteInput> input = showNoteForm(
                                line.studentProperty().get(),
                                line.courseProperty().get(),
                                line.scoreProperty().get(),
                                line.coefProperty().get()
                            );
                            input.ifPresent(data -> {
                                line.scoreProperty().set(data.score());
                                line.coefProperty().set(data.coef());
                                
                                // Persister les modifications en base de données
                                if (line.getId() > 0) {
                                    try {
                                        models.Note note = new models.Note(line.getId(), line.getStudentId(), line.getCourseId(), data.score(), data.coef(), LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                                        state.notesDao.update(note);
                                    } catch (Exception e) {
                                        showInfo("Erreur", "Erreur lors de la mise à jour : " + e.getMessage());
                                    }
                                }
                                
                                table.refresh();
                            });
                        }
                    });
                    delete.setOnAction(event -> {
                        NoteLine line = getItem();
                        if (line != null && showConfirm("Confirmation", "Supprimer cette note ?")) {
                            // Supprimer de la base de données
                            if (line.getId() > 0) {
                                try {
                                    state.notesDao.deleteById(line.getId());
                                } catch (Exception e) {
                                    showInfo("Erreur", "Erreur lors de la suppression : " + e.getMessage());
                                }
                            }
                            state.notes.remove(line);
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
        } else {
            table.getColumns().setAll(studentCol, courseCol, noteCol, coefCol);
        }

        Button addLine = new Button("+ Ajouter note");
        addLine.getStyleClass().addAll("action-button", "primary-action");
        addLine.setOnAction(event -> {
            if (state.courses.isEmpty()) {
                showInfo("Aucun cours", "Aucun cours ne vous est assigne pour le moment.");
                return;
            }
            if (state.students.isEmpty()) {
                showInfo("Aucun etudiant", "Aucun etudiant disponible.");
                return;
            }

            ComboBox<Student> studentCombo = new ComboBox<>(state.students);
            studentCombo.setPromptText("Selectionner un etudiant");
            studentCombo.setPrefWidth(300);
            studentCombo.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(Student item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.matricule() + " - " + item.fullName());
                }
            });
            studentCombo.setButtonCell(studentCombo.getCellFactory().call(null));

            ComboBox<Course> courseCombo = new ComboBox<>(state.courses);
            courseCombo.setPromptText("Selectionner un cours");
            courseCombo.setPrefWidth(300);
            courseCombo.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.title());
                }
            });
            courseCombo.setButtonCell(courseCombo.getCellFactory().call(null));

            Dialog<NoteInput> dialog = createEntityDialog("Ajouter une note", "Remplir les informations de la note");
            ButtonType saveType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
            styleDialogButtons(dialog, saveType);

            GridPane grid = formGrid();
            addFormRow(grid, 0, "Etudiant", studentCombo);
            addFormRow(grid, 1, "Cours", courseCombo);

            TextField scoreField = new TextField("12.0");
            scoreField.setPromptText("Note (0-20)");
            addFormRow(grid, 2, "Note", scoreField);

            TextField coefField = new TextField("2.0");
            coefField.setPromptText("Coefficient");
            addFormRow(grid, 3, "Coefficient", coefField);

            dialog.getDialogPane().setContent(grid);

            Node saveButton = dialog.getDialogPane().lookupButton(saveType);
            saveButton.disableProperty().bind(
                studentCombo.valueProperty().isNull()
                    .or(courseCombo.valueProperty().isNull())
                    .or(scoreField.textProperty().isEmpty())
                    .or(coefField.textProperty().isEmpty())
            );

            dialog.setResultConverter(button -> {
                if (button == saveType) {
                    try {
                        Student student = studentCombo.getValue();
                        Course course = courseCombo.getValue();
                        String studentName = student.fullName();
                        String courseName = course.title();

                        // Vérifier les doublons
                        for (NoteLine note : state.notes) {
                            if (note.studentProperty().get().equalsIgnoreCase(studentName)
                                    && note.courseProperty().get().equalsIgnoreCase(courseName)) {
                                showInfo("Doublon detecte", "Une note existe déjà pour cet étudiant dans ce cours.");
                                return null;
                            }
                        }

                        double score = Double.parseDouble(scoreField.getText().trim());
                        double coef = Double.parseDouble(coefField.getText().trim());

                        if (score < 0 || score > 20) {
                            showInfo("Note invalide", "La note doit etre entre 0 et 20.");
                            return null;
                        }

                        if (coef <= 0) {
                            showInfo("Coefficient invalide", "Le coefficient doit etre positif.");
                            return null;
                        }

                        return new NoteInput(studentName, courseName, score, coef);
                    } catch (NumberFormatException e) {
                        showInfo("Format invalide", "Veuillez entrer des nombres valides.");
                        return null;
                    }
                }
                return null;
            });

            Optional<NoteInput> result = dialog.showAndWait();
            result.ifPresent(data -> {
                try {
                    // Chercher les IDs dans la base de données
                    Student selectedStudent = studentCombo.getValue();
                    Course selectedCourse = courseCombo.getValue();
                    
                    if (selectedStudent != null && selectedCourse != null) {
                        // Chercher l'étudiant et le cours dans la base
                        Optional<models.Etudiant> etudiantOpt = state.etudiantDao.findAll().stream()
                            .filter(e -> e.getMatricule().equals(selectedStudent.matricule()))
                            .findFirst();
                        
                        Optional<models.Cours> coursOpt = state.coursDao.findAll().stream()
                            .filter(c -> c.getIntitule().equals(selectedCourse.title()))
                            .findFirst();
                        
                        if (etudiantOpt.isPresent() && coursOpt.isPresent()) {
                            models.Etudiant etudiant = etudiantOpt.get();
                            models.Cours cours = coursOpt.get();
                            
                            // Créer et persister la note
                            String dateCreation = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
                            models.Note note = new models.Note(etudiant.getId(), cours.getId(), data.score(), data.coef(), dateCreation);
                            state.notesDao.save(note);
                            
                            // Ajouter à l'interface avec les IDs
                            state.notes.add(new NoteLine(note.getId(), etudiant.getId(), cours.getId(), data.student(), data.course(), data.score(), data.coef()));
                        }
                    }
                } catch (Exception e) {
                    showInfo("Erreur", "Erreur lors de l'enregistrement de la note : " + e.getMessage());
                }
            });
        });

        if (state.isStudentMode() || state.isAdminMode()) {
            page.getChildren().addAll(table);
        } else {
            page.getChildren().addAll(addLine, table);
        }
        return page;
    }

    private static Optional<NoteInput> showNoteForm(String student, String course, double score, double coef) {
        Dialog<NoteInput> dialog = createEntityDialog("Modifier la note", "Mettre a jour les informations");
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        styleDialogButtons(dialog, saveType);

        Label studentLabel = new Label(student);
        studentLabel.getStyleClass().add("form-label");
        Label courseLabel = new Label(course);
        courseLabel.getStyleClass().add("form-label");

        TextField scoreField = new TextField(String.valueOf(score));
        scoreField.setPromptText("Note (0-20)");

        TextField coefField = new TextField(String.valueOf(coef));
        coefField.setPromptText("Coefficient");

        GridPane grid = formGrid();
        addFormRow(grid, 0, "Etudiant", studentLabel);
        addFormRow(grid, 1, "Cours", courseLabel);
        addFormRow(grid, 2, "Note", scoreField);
        addFormRow(grid, 3, "Coefficient", coefField);

        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        saveButton.disableProperty().bind(
            scoreField.textProperty().isEmpty()
                .or(coefField.textProperty().isEmpty())
        );

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                try {
                    double newScore = Double.parseDouble(scoreField.getText().trim());
                    double newCoef = Double.parseDouble(coefField.getText().trim());

                    if (newScore < 0 || newScore > 20) {
                        showInfo("Note invalide", "La note doit etre entre 0 et 20.");
                        return null;
                    }

                    if (newCoef <= 0) {
                        showInfo("Coefficient invalide", "Le coefficient doit etre positif.");
                        return null;
                    }

                    return new NoteInput(student, course, newScore, newCoef);
                } catch (NumberFormatException e) {
                    showInfo("Format invalide", "Veuillez entrer des nombres valides.");
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
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

        if (state.isStudentMode()) {
            Student selected = state.currentStudent();
            if (selected != null) {
                studentCombo.setValue(selected);
                studentCombo.setDisable(true);
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
            }
        }

        if (state.isTeacherMode()) {
            studentCombo.setPromptText("Selectionner un etudiant de vos cours...");
        }

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
        addSession.setVisible(state.isAdminMode());

        HBox topRow = new HBox(8, filter, addSession);
        topRow.setAlignment(Pos.CENTER_LEFT);

        TableView<SessionLine> table = new TableView<>(state.sessions);
        table.getStyleClass().add("data-table");

        TableColumn<SessionLine, String> dayCol = textColumn("JOUR", SessionLine::dayProperty, 100);
        TableColumn<SessionLine, String> startCol = textColumn("DEBUT", SessionLine::startProperty, 80);
        TableColumn<SessionLine, String> endCol = textColumn("FIN", SessionLine::endProperty, 80);
        TableColumn<SessionLine, String> courseCol = textColumn("SEANCE", SessionLine::courseProperty, 180);
        TableColumn<SessionLine, String> roomCol = textColumn("SALLE", SessionLine::roomProperty, 120);
        TableColumn<SessionLine, String> teacherCol = textColumn("ENSEIGNANT", SessionLine::teacherProperty, 140);

        if (state.isAdminMode()) {
            TableColumn<SessionLine, SessionLine> actionCol = new TableColumn<>("ACTIONS");
            actionCol.setPrefWidth(160);
            actionCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
            actionCol.setCellFactory(col -> new TableCell<>() {
                private final Button edit = new Button("Editer");
                private final Button delete = new Button("Supprimer");
                private final HBox box = new HBox(6, edit, delete);

                {
                    edit.getStyleClass().add("mini-action");
                    delete.getStyleClass().add("mini-danger");
                    edit.setOnAction(event -> {
                        SessionLine line = getItem();
                        if (line != null) {
                            Optional<SessionInput> input = showSessionForm(line, state);
                            input.ifPresent(data -> {
                                try {
                                    // Find the database entities
                                    models.Groupe groupe = state.groupeDao.findById(data.groupeId()).orElse(null);
                                    models.Enseignant enseignant = state.enseignantDao.findById(data.enseignantId()).orElse(null);
                                    models.Salle salle = state.salleDao.findAll().stream()
                                        .filter(s -> s.getIdentifiantSalle().equals(data.room()))
                                        .findFirst().orElse(null);
                                    models.Cours cours = state.coursDao.findAll().stream()
                                        .filter(c -> c.getIntitule().equals(data.course()))
                                        .findFirst().orElse(null);
                                    
                                    if (groupe != null && enseignant != null && salle != null && cours != null) {
                                        // Create and save the seance to database
                                        models.Seance seance = new models.Seance(
                                            "SEA-" + System.currentTimeMillis(),
                                            parseDay(data.day()).toString(),
                                            data.start(),
                                            data.end(),
                                            groupe,
                                            enseignant,
                                            salle,
                                            cours
                                        );
                                        state.seanceDao.update(seance);
                                        
                                        // Update the UI
                                        int index = state.sessions.indexOf(line);
                                        if (index >= 0) {
                                            state.sessions.set(index, new SessionLine(line.seanceId(), data.day(), data.start(), data.end(), data.course(), data.room(), data.teacher(), data.groupeId(), data.enseignantId()));
                                            table.refresh();
                                        }
                                        showInfo("Succes", "Seance mise a jour avec succes");
                                    } else {
                                        showInfo("Erreur", "Impossible de trouver les donnees requises pour la seance");
                                    }
                                } catch (Exception e) {
                                    showInfo("Erreur", "Erreur lors de la mise a jour: " + e.getMessage());
                                }
                            });
                        }
                    });
                    delete.setOnAction(event -> {
                        SessionLine line = getItem();
                        if (line != null && showConfirm("Confirmation", "Supprimer cette seance ?")) {
                            try {
                                // Delete from database
                                if (line.seanceId() > 0) {
                                    state.seanceDao.deleteById(line.seanceId());
                                }
                                state.sessions.remove(line);
                                showInfo("Succes", "Seance supprimee avec succes");
                            } catch (Exception e) {
                                showInfo("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                            }
                        }
                    });
                }

                @Override
                protected void updateItem(SessionLine item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty || item == null ? null : box);
                }
            });
            table.getColumns().setAll(dayCol, startCol, endCol, courseCol, roomCol, teacherCol, actionCol);
        } else {
            table.getColumns().setAll(dayCol, startCol, endCol, courseCol, roomCol, teacherCol);
        }

        FilteredList<SessionLine> filtered = new FilteredList<>(state.sessions, line -> true);
        table.setItems(filtered);

        filter.setOnAction(event -> {
            String choice = filter.getValue();
            filtered.setPredicate(line -> "Tout afficher".equals(choice) || line.dayProperty().get().equalsIgnoreCase(choice));
        });

        addSession.setOnAction(event -> {
            if (!state.isAdminMode()) {
                showInfo("Action non autorisee", "Seul le chef de departement (admin) peut planifier les seances.");
                return;
            }

            Optional<SessionInput> result = showSessionForm(null, state);
            result.ifPresent(data -> {
                try {
                    // Find the database entities
                    models.Groupe groupe = state.groupeDao.findById(data.groupeId()).orElse(null);
                    models.Enseignant enseignant = state.enseignantDao.findById(data.enseignantId()).orElse(null);
                    models.Salle salle = state.salleDao.findAll().stream()
                        .filter(s -> s.getIdentifiantSalle().equals(data.room()))
                        .findFirst().orElse(null);
                    models.Cours cours = state.coursDao.findAll().stream()
                        .filter(c -> c.getIntitule().equals(data.course()))
                        .findFirst().orElse(null);
                    
                    if (groupe != null && enseignant != null && salle != null && cours != null) {
                        // Create and save the seance to database
                        models.Seance seance = new models.Seance(
                            "SEA-" + System.currentTimeMillis(),
                            parseDay(data.day()).toString(),
                            data.start(),
                            data.end(),
                            groupe,
                            enseignant,
                            salle,
                            cours
                        );
                        models.Seance saved = state.seanceDao.save(seance);
                        
                        // Add to UI with database ID
                        state.sessions.add(new SessionLine(saved.getId(), data.day(), data.start(), data.end(), data.course(), data.room(), data.teacher(), data.groupeId(), data.enseignantId()));
                        showInfo("Succes", "Seance creee avec succes");
                    } else {
                        showInfo("Erreur", "Impossible de trouver les donnees requises pour la seance");
                    }
                } catch (Exception e) {
                    showInfo("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
                }
            });
        });

        page.getChildren().addAll(topRow, table);
        return page;
    }

    private static Optional<SessionInput> showSessionForm(SessionLine current, DashboardState state) {
        Dialog<SessionInput> dialog = createEntityDialog(
            current == null ? "Ajouter une seance" : "Modifier la seance",
            current == null ? "Creer une nouvelle seance planifiee" : "Modifier les informations de la seance"
        );

        ButtonType saveType = new ButtonType(current == null ? "Ajouter" : "Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        styleDialogButtons(dialog, saveType);

        ComboBox<String> dayCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"
        ));
        dayCombo.setValue(current == null ? "Lundi" : current.day());
        dayCombo.setPrefWidth(250);

        TextField startField = new TextField(current == null ? "08:00" : current.start());
        startField.setPromptText("Heure debut (HH:MM)");

        TextField endField = new TextField(current == null ? "10:00" : current.end());
        endField.setPromptText("Heure fin (HH:MM)");

        // Dropdown for Groups
        ComboBox<GroupLine> groupCombo = new ComboBox<>(state.groups);
        groupCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(GroupLine item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name() + " (" + item.level() + " " + item.track() + ")");
            }
        });
        groupCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(GroupLine item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name() + " (" + item.level() + " " + item.track() + ")");
            }
        });

        // Dropdown for Courses
        ComboBox<Course> courseCombo = new ComboBox<>(state.courses);
        courseCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.title());
            }
        });
        courseCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.title());
            }
        });
        if (current != null) {
            state.courses.stream()
                .filter(c -> c.title().equals(current.course()))
                .findFirst()
                .ifPresent(courseCombo::setValue);
        }

        // Dropdown for Rooms
        ComboBox<Room> roomCombo = new ComboBox<>(state.rooms);
        roomCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Room item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name() + " (" + item.type() + ")");
            }
        });
        roomCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Room item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name() + " (" + item.type() + ")");
            }
        });
        if (current != null) {
            state.rooms.stream()
                .filter(r -> r.name().equals(current.room()))
                .findFirst()
                .ifPresent(roomCombo::setValue);
        }

        // Dropdown for Teachers
        ComboBox<Teacher> teacherCombo = new ComboBox<>(state.teachers);
        teacherCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Teacher item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.lastName() + " " + item.firstName());
            }
        });
        teacherCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Teacher item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.lastName() + " " + item.firstName());
            }
        });
        if (current != null) {
            state.teachers.stream()
                .filter(t -> (t.lastName() + " " + t.firstName()).equals(current.teacher()))
                .findFirst()
                .ifPresent(teacherCombo::setValue);
        }

        GridPane grid = formGrid();
        addFormRow(grid, 0, "Jour", dayCombo);
        addFormRow(grid, 1, "Groupe", groupCombo);
        addFormRow(grid, 2, "Debut", startField);
        addFormRow(grid, 3, "Fin", endField);
        addFormRow(grid, 4, "Seance", courseCombo);
        addFormRow(grid, 5, "Salle", roomCombo);
        addFormRow(grid, 6, "Enseignant", teacherCombo);

        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        saveButton.disableProperty().bind(
            dayCombo.valueProperty().isNull()
                .or(groupCombo.valueProperty().isNull())
                .or(startField.textProperty().isEmpty())
                .or(endField.textProperty().isEmpty())
                .or(courseCombo.valueProperty().isNull())
                .or(roomCombo.valueProperty().isNull())
                .or(teacherCombo.valueProperty().isNull())
        );

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                String start = startField.getText().trim();
                String end = endField.getText().trim();

                // Validation du format HH:MM
                if (!start.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                    showInfo("Format invalide", "L'heure de debut doit etre au format HH:MM");
                    return null;
                }
                if (!end.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                    showInfo("Format invalide", "L'heure de fin doit etre au format HH:MM");
                    return null;
                }

                Course selectedCourse = courseCombo.getValue();
                Room selectedRoom = roomCombo.getValue();
                Teacher selectedTeacher = teacherCombo.getValue();
                GroupLine selectedGroup = groupCombo.getValue();

                if (selectedCourse == null || selectedRoom == null || selectedTeacher == null || selectedGroup == null) {
                    showInfo("Erreur", "Veuillez selectionner tous les champs requis.");
                    return null;
                }

                return new SessionInput(
                    dayCombo.getValue(),
                    start,
                    end,
                    selectedCourse.title(),
                    selectedRoom.name(),
                    selectedTeacher.lastName() + " " + selectedTeacher.firstName(),
                    selectedGroup.id(),  // groupeId from selected group
                    selectedTeacher.id()  // enseignantId from selected teacher
                );
            }
            return null;
        });

        return dialog.showAndWait();
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

    private static <T> VBox buildReadOnlyDataPage(
        String title,
        String searchPrompt,
        ObservableList<T> source,
        TableView<T> table,
        QueryMatcher<T> matcher
    ) {
        VBox page = createPageShell(title, "Donnees personnelles");

        TextField search = new TextField();
        search.setPromptText(searchPrompt);
        search.getStyleClass().add("search-field");
        search.setPrefWidth(340);

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

        page.getChildren().addAll(search, table);
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
        EditHandler<T> onEdit,
        DeleteHandler<T> onDelete
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
                            if (onDelete.delete(item)) {
                                source.remove(item);
                            }
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

    private static Optional<Course> showCourseForm(Course current, ObservableList<Teacher> teachers) {
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

        ComboBox<Teacher> enseignant = new ComboBox<>(teachers);
        enseignant.setPromptText("Selectionner un enseignant");
        enseignant.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Teacher item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.fullName() + " - " + item.email());
            }
        });
        enseignant.setButtonCell(enseignant.getCellFactory().call(null));

        if (current != null && current.teacher() != null && !current.teacher().isBlank()) {
            String currentTeacher = current.teacher().trim().toLowerCase();
            teachers.stream()
                .filter(t -> t.fullName().toLowerCase().equals(currentTeacher) || t.lastName().toLowerCase().equals(currentTeacher))
                .findFirst()
                .ifPresent(enseignant::setValue);
        }

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
                .or(enseignant.valueProperty().isNull())
        );

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                Teacher selected = enseignant.getValue();
                String teacherName = selected == null ? "" : selected.fullName();
                return new Course(code.getText().trim(), intitule.getText().trim(), volume.getText().trim(), teacherName);
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
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("Amphitheatre", "Salle TP", "Salle Cours"));
        type.setPromptText("Type de salle");
        if (current != null && current.type() != null && !current.type().isBlank()) {
            type.setValue(current.type());
        }
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
                .or(type.valueProperty().isNull())
                .or(capacity.textProperty().isEmpty())
        );

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                return new Room(id.getText().trim(), type.getValue(), capacity.getText().trim());
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private static Optional<CredentialInput> showCredentialForm(String title, String defaultIdentifier, String passwordHint) {
        Dialog<CredentialInput> dialog = createEntityDialog(title, "Configurer les identifiants de connexion.");

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        styleDialogButtons(dialog, saveType);

        TextField identifier = new TextField(defaultIdentifier == null ? "" : defaultIdentifier.trim());
        identifier.setPromptText("Identifiant de connexion");

        TextField password = new TextField();
        password.setPromptText(passwordHint);

        GridPane grid = formGrid();
        addFormRow(grid, 0, "Identifiant", identifier);
        addFormRow(grid, 1, "Mot de passe", password);
        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        saveButton.disableProperty().bind(identifier.textProperty().isEmpty());

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                return new CredentialInput(identifier.getText().trim(), password.getText().trim());
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

    private static models.Etudiant toEtudiantEntity(Student student) {
        int year = parsePositiveInt(student.year(), 1);
        return new models.Etudiant(
            student.lastName(),
            student.firstName(),
            student.matricule().toLowerCase() + "@enspd.cm",
            "2000-01-01",
            student.matricule(),
            student.track(),
            year
        );
    }

    private static models.Enseignant toEnseignantEntity(Teacher teacher) {
        return new models.Enseignant(
            teacher.lastName(),
            teacher.firstName(),
            teacher.email(),
            "1985-01-01",
            teacher.department(),
            fromTeacherStatus(teacher.status())
        );
    }

    private static models.Cours toCoursEntity(Course course, ObservableList<Teacher> teachers, EnseignantDao enseignantDao) {
        models.Enseignant responsable = null;
        if (course.teacher() != null && !course.teacher().isBlank()) {
            String value = course.teacher().trim().toLowerCase();
            for (Teacher teacher : teachers) {
                String full = (teacher.lastName() + " " + teacher.firstName()).toLowerCase();
                if (full.contains(value) || value.contains(teacher.lastName().toLowerCase())) {
                    models.Enseignant entity = toEnseignantEntity(teacher);
                    if (teacher.id() > 0) {
                        entity.setId(teacher.id());
                    } else {
                        Optional<models.Enseignant> existing = enseignantDao.findAll().stream()
                            .filter(e -> e.getEmail().equalsIgnoreCase(teacher.email()))
                            .findFirst();
                        if (existing.isPresent()) {
                            entity.setId(existing.get().getId());
                        }
                    }
                    responsable = entity;
                    break;
                }
            }
        }

        int volume = parsePositiveInt(course.volume().replace("h", "").trim(), 1);
        return new models.Cours(course.code(), course.title(), "Description du cours", volume, responsable);
    }

    private static models.Groupe toGroupeEntity(GroupLine group) {
        int capacity = parsePositiveInt(group.size(), 20);
        TypeGroupe type = fromGroupType(group.level(), group.name());
        return new models.Groupe(group.name(), capacity, null, type);
    }

    private static models.Salle toSalleEntity(Room room) {
        int capacity = parsePositiveInt(room.capacity().replace("places", "").trim(), 20);
        return new models.Salle(room.name(), capacity, fromRoomType(room.type()));
    }

    private static TypeSalle fromRoomType(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase();
        if (normalized.contains("amphi")) {
            return TypeSalle.AMPHI;
        }
        if (normalized.contains("tp")) {
            return TypeSalle.SALLE_TP;
        }
        return TypeSalle.CLASSIQUE;
    }

    private static String toRoomType(TypeSalle type) {
        return switch (type) {
            case AMPHI -> "Amphitheatre";
            case SALLE_TP -> "Salle TP";
            case CLASSIQUE -> "Salle Cours";
        };
    }

    private static TypeGroupe fromGroupType(String level, String name) {
        String normalized = ((level == null ? "" : level) + " " + (name == null ? "" : name)).toLowerCase();
        if (normalized.contains("tp")) {
            return TypeGroupe.TP;
        }
        if (normalized.contains("td")) {
            return TypeGroupe.TD;
        }
        return TypeGroupe.CM;
    }

    private static StatutEnseignant fromTeacherStatus(String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase();
        if (normalized.contains("vacataire")) {
            return StatutEnseignant.VACATAIRE;
        }
        if (normalized.contains("assistant")) {
            return StatutEnseignant.ASSISTANT;
        }
        return StatutEnseignant.TITULAIRE;
    }

    private static String toTeacherStatus(StatutEnseignant status) {
        return switch (status) {
            case VACATAIRE -> "Vacataire";
            case ASSISTANT -> "Assistant";
            case TITULAIRE -> "Permanent";
        };
    }

    private static int parsePositiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value.replaceAll("[^0-9]", ""));
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static <T> void replaceById(ObservableList<T> source, T value, IdAccessor<T> idAccessor) {
        int index = -1;
        for (int i = 0; i < source.size(); i++) {
            if (idAccessor.id(source.get(i)) == idAccessor.id(value)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            source.set(index, value);
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

    private static boolean showConfirm(String title, String content) {
        Dialog<ButtonType> dialog = new Dialog<>();
        applyDialogTheme(dialog);
        dialog.setTitle(title);
        dialog.getDialogPane().getStyleClass().add("message-dialog");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

        Label icon = new Label("?");
        icon.getStyleClass().addAll("message-icon", "message-warning");

        Label heading = new Label(title);
        heading.getStyleClass().add("message-title");

        Label body = new Label(content);
        body.getStyleClass().add("message-body");
        body.setWrapText(true);

        VBox contentBox = new VBox(8, icon, heading, body);
        contentBox.getStyleClass().add("message-box");
        dialog.getDialogPane().setContent(contentBox);

        Node yesButton = dialog.getDialogPane().lookupButton(ButtonType.YES);
        yesButton.getStyleClass().add("dialog-primary-button");
        Node noButton = dialog.getDialogPane().lookupButton(ButtonType.NO);
        noButton.getStyleClass().add("dialog-secondary-button");

        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private static void runMockAction(Button button, String loadingLabel, Runnable action) {
        String initialText = button.getText();
        button.setDisable(true);
        button.setText(loadingLabel);

        try {
            action.run();
        } catch (RuntimeException ex) {
            String message = ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Operation impossible. Verifiez les donnees saisies."
                : ex.getMessage();
            showInfo("Operation echouee", message);
        } finally {
            button.setText(initialText);
            button.setDisable(false);
        }
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

    @FunctionalInterface
    private interface DeleteHandler<T> {
        boolean delete(T value);
    }

    @FunctionalInterface
    private interface IdAccessor<T> {
        int id(T value);
    }

    private record Student(int id, String matricule, String lastName, String firstName, String year, String track) {
        private Student(String matricule, String lastName, String firstName, String year, String track) {
            this(0, matricule, lastName, firstName, year, track);
        }

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

    private record Teacher(int id, String lastName, String firstName, String email, String status, String department) {
        private Teacher(String lastName, String firstName, String email, String status, String department) {
            this(0, lastName, firstName, email, status, department);
        }

        String fullName() {
            return lastName + " " + firstName;
        }

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

    private record Course(int id, String code, String title, String volume, String teacher) {
        private Course(String code, String title, String volume, String teacher) {
            this(0, code, title, volume, teacher);
        }

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

    private record GroupLine(int id, String name, String level, String track, String size) {
        private GroupLine(String name, String level, String track, String size) {
            this(0, name, level, track, size);
        }

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

    private record Room(int id, String name, String type, String capacity) {
        private Room(String name, String type, String capacity) {
            this(0, name, type, capacity);
        }

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

    private record CredentialInput(String identifier, String password) {
    }

    private record NoteInput(String student, String course, double score, double coef) {
    }

    private record SessionInput(String day, String start, String end, String course, String room, String teacher, int groupeId, int enseignantId) {
        // Backward compatibility constructor
        SessionInput(String day, String start, String end, String course, String room, String teacher) {
            this(day, start, end, course, room, teacher, -1, -1);
        }
    }

    private static final class NoteLine {
        private final int id;
        private final int studentId;
        private final int courseId;
        private final SimpleStringProperty student;
        private final SimpleStringProperty course;
        private final DoubleProperty score;
        private final DoubleProperty coef;

        private NoteLine(String student, String course, double score, double coef) {
            this(0, 0, 0, student, course, score, coef);
        }

        private NoteLine(int id, int studentId, int courseId, String student, String course, double score, double coef) {
            this.id = id;
            this.studentId = studentId;
            this.courseId = courseId;
            this.student = new SimpleStringProperty(student);
            this.course = new SimpleStringProperty(course);
            this.score = new SimpleDoubleProperty(score);
            this.coef = new SimpleDoubleProperty(coef);
        }

        int getId() {
            return id;
        }

        int getStudentId() {
            return studentId;
        }

        int getCourseId() {
            return courseId;
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

    private record SessionLine(int seanceId, String day, String start, String end, String course, String room, String teacher, int groupeId, int enseignantId) {
        
        // Constructor without IDs for backward compatibility (for new sessions)
        SessionLine(String day, String start, String end, String course, String room, String teacher) {
            this(0, day, start, end, course, room, teacher, -1, -1);
        }

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
        private final String connectedUsername;
        private final Integer linkedEntityId;
        private final ObservableList<Module> allowedModules;
        private final StackPane pageContainer;
        private final Map<Module, Node> pageCache;
        private final Map<Module, Button> navButtons;

        private final EtudiantDao etudiantDao;
        private final EnseignantDao enseignantDao;
        private final CoursDao coursDao;
        private final GroupeDao groupeDao;
        private final SalleDao salleDao;
        private final NotesDao notesDao;
        private final SeanceDao seanceDao;
        private final InscriptionDao inscriptionDao;
        private final AuthService authService;

        private final ObservableList<Student> students;
        private final ObservableList<Teacher> teachers;
        private final ObservableList<Course> courses;
        private final ObservableList<GroupLine> groups;
        private final ObservableList<Room> rooms;
        private final ObservableList<NoteLine> notes;
        private final ObservableList<SessionLine> sessions;
        private final ObservableList<String> recentEnrollments;
        private final ObservableList<String> nextSessions;
        private final Student currentStudent;
        private final Teacher currentTeacher;

        private final AtomicInteger studentCounter;
        private Module activeModule;

        private DashboardState(String roleTitle, String connectedUsername, Integer linkedEntityId) {
            this.roleTitle = roleTitle;
            this.connectedUsername = connectedUsername;
            this.linkedEntityId = linkedEntityId;
            this.allowedModules = modulesForRole(roleTitle);
            this.pageContainer = new StackPane();
            this.pageContainer.getStyleClass().add("page-container");
            this.pageCache = new EnumMap<>(Module.class);
            this.navButtons = new EnumMap<>(Module.class);

            this.etudiantDao = new EtudiantDao();
            this.enseignantDao = new EnseignantDao();
            this.coursDao = new CoursDao();
            this.groupeDao = new GroupeDao();
            this.salleDao = new SalleDao();
            this.notesDao = new NotesDao();
            this.seanceDao = new SeanceDao();
            this.inscriptionDao = new InscriptionDao();
            this.authService = new AuthService();

            seedIfEmpty();

            this.students = FXCollections.observableArrayList(loadStudents());
            this.teachers = FXCollections.observableArrayList(loadTeachers());
            this.courses = FXCollections.observableArrayList(loadCourses());
            this.groups = FXCollections.observableArrayList(loadGroups());
            this.rooms = FXCollections.observableArrayList(loadRooms());
            this.currentStudent = resolveCurrentStudent();
            this.currentTeacher = resolveCurrentTeacher();

            // Charger les notes depuis la base de données
            List<NoteLine> loadedNotes = loadNotesFromDatabase();

            this.notes = isStudentMode() && currentStudent != null
                ? FXCollections.observableArrayList(filterNotesForStudent(loadedNotes, currentStudent))
                : FXCollections.observableArrayList(loadedNotes);

            // Charger les séances depuis la base de données
            List<SessionLine> loadedSessions = loadSessionsFromDatabase();

            this.sessions = isStudentMode() && currentStudent != null
                ? FXCollections.observableArrayList(filterSessionsForStudent(loadedSessions, currentStudent))
                : (isTeacherMode() && currentTeacher != null
                    ? FXCollections.observableArrayList(filterSessionsForTeacher(loadedSessions, currentTeacher, courses))
                    : FXCollections.observableArrayList(loadedSessions));

            ObservableList<String> seededEnrollments = FXCollections.observableArrayList(
                "Mac Arthur EDAMBO - Merise - TD1",
                "Jordan Mateo BALLA - Merise - TD1"
            );

            this.recentEnrollments = isStudentMode() && currentStudent != null
                ? FXCollections.observableArrayList(filterEnrollmentsForStudent(seededEnrollments, currentStudent))
                : seededEnrollments;

            ObservableList<String> seededNextSessions = FXCollections.observableArrayList(
                "Info 214 TD1 - 20:25 a 10:20",
                "Info 214 TD1 - 10:00 a 11:00",
                "Info 214 TD1 - 06:00 a 07:00"
            );

            this.nextSessions = isStudentMode() && currentStudent != null
                ? FXCollections.observableArrayList(filterNextSessionsForStudent(seededNextSessions, this.notes))
                : seededNextSessions;

            if (isStudentMode() && !this.notes.isEmpty()) {
                Set<String> ownCourseTitles = new HashSet<>();
                for (NoteLine note : this.notes) {
                    ownCourseTitles.add(note.courseProperty().get().toLowerCase());
                }
                this.courses.removeIf(course -> !ownCourseTitles.contains(course.title().toLowerCase()));
            }

            if (isTeacherMode() && currentTeacher != null) {
                List<Course> teacherCourses = filterCoursesForTeacher(this.courses, currentTeacher);
                this.courses.setAll(teacherCourses);

                this.notes.setAll(filterNotesForTeacher(this.notes, this.courses));
                this.sessions.setAll(filterSessionsForTeacher(this.sessions, currentTeacher, this.courses));
                this.recentEnrollments.setAll(filterEnrollmentsForTeacher(this.recentEnrollments, this.courses));
                this.nextSessions.setAll(filterNextSessionsForTeacher(this.nextSessions, this.courses));
                // Les étudiants sont affichés sans filtrage pour permettre à l'enseignant d'ajouter des notes
            }

            this.studentCounter = new AtomicInteger(students.size() + 1);
        }

        private void seedIfEmpty() {
            if (enseignantDao.findAll().isEmpty()) {
                enseignantDao.save(new models.Enseignant("NGAFFO", "Armelle", "armelle.ngaffo@enspd.cm", "1985-02-03", "Informatique", StatutEnseignant.TITULAIRE));
                enseignantDao.save(new models.Enseignant("TALLA", "Emmanuel", "emmanuel.talla@enspd.cm", "1984-06-12", "Informatique", StatutEnseignant.TITULAIRE));
                enseignantDao.save(new models.Enseignant("NDJOCK", "Blaise", "blaise.ndjock@enspd.cm", "1987-11-05", "Mathematiques", StatutEnseignant.VACATAIRE));
            }

            if (etudiantDao.findAll().isEmpty()) {
                etudiantDao.save(new models.Etudiant("BALLA", "Jordan Mateo", "23900030@enspd.cm", "2003-03-14", "23900030", "Genie Informatique", 3));
                etudiantDao.save(new models.Etudiant("EDAMBO", "Mac Arthur", "23900060@enspd.cm", "2003-08-10", "23900060", "Genie Informatique", 3));
            }

            if (coursDao.findAll().isEmpty()) {
                models.Enseignant responsable = enseignantDao.findAll().stream().findFirst().orElse(null);
                coursDao.save(new models.Cours("info 214", "Merise", "Modelisation UML", 25, responsable));
                coursDao.save(new models.Cours("info 218", "Systeme d'exploitation", "Architecture OS", 40, responsable));
            }

            if (groupeDao.findAll().isEmpty()) {
                groupeDao.save(new models.Groupe("TD1", 35, null, TypeGroupe.TD));
                groupeDao.save(new models.Groupe("TP1", 20, null, TypeGroupe.TP));
            }

            if (salleDao.findAll().isEmpty()) {
                salleDao.save(new models.Salle("Amphi 101", 100, TypeSalle.AMPHI));
                salleDao.save(new models.Salle("10BP1", 40, TypeSalle.SALLE_TP));
            }
        }

        private List<Student> loadStudents() {
            return etudiantDao.findAll().stream()
                .map(e -> new Student(e.getId(), e.getMatricule(), e.getNom(), e.getPrenom(), Integer.toString(e.getAnnee()), e.getFiliere()))
                .toList();
        }

        private List<Teacher> loadTeachers() {
            return enseignantDao.findAll().stream()
                .map(e -> new Teacher(e.getId(), e.getNom(), e.getPrenom(), e.getEmail(), toTeacherStatus(e.getStatut()), e.getDepartement()))
                .toList();
        }

        private List<Course> loadCourses() {
            return coursDao.findAll().stream()
                .map(c -> {
                    String teacher = c.getEnseignantResponsable() == null ? "A valider" : c.getEnseignantResponsable().getNom() + " " + c.getEnseignantResponsable().getPrenom();
                    return new Course(c.getId(), c.getCode(), c.getIntitule(), c.getVolumeHoraire() + "h", teacher);
                })
                .toList();
        }

        private List<GroupLine> loadGroups() {
            return groupeDao.findAll().stream()
                .map(g -> new GroupLine(g.getId(), g.getNom(), g.getType().name(), "General", Integer.toString(g.getCapacite())))
                .toList();
        }

        private List<Room> loadRooms() {
            return salleDao.findAll().stream()
                .map(s -> new Room(s.getId(), s.getIdentifiantSalle(), toRoomType(s.getTypeSalle()), s.getCapaciteAccueil() + " places"))
                .toList();
        }

        private List<NoteLine> loadNotesFromDatabase() {
            List<NoteLine> noteLines = new ArrayList<>();
            try {
                List<models.Note> databaseNotes = notesDao.findAll();
                for (models.Note note : databaseNotes) {
                    models.Etudiant student = etudiantDao.findById(note.getEtudiantId()).orElse(null);
                    models.Cours course = coursDao.findById(note.getCoursId()).orElse(null);
                    
                    if (student != null && course != null) {
                        String studentName = student.getPrenom() + " " + student.getNom();
                        noteLines.add(new NoteLine(note.getId(), student.getId(), course.getId(), studentName, course.getIntitule(), note.getScore(), note.getCoefficient()));
                    }
                }
            } catch (Exception e) {
                // Si erreur, retourner liste vide (notes en mémoire seulement)
                System.err.println("Erreur chargement notes: " + e.getMessage());
            }
            return noteLines;
        }

        private List<SessionLine> loadSessionsFromDatabase() {
            List<SessionLine> sessionLines = new ArrayList<>();
            try {
                List<models.Seance> databaseSeances = seanceDao.findAll();
                for (models.Seance seance : databaseSeances) {
                    String day = getJourFrancais(seance.getDate().getDayOfWeek().name());
                    String start = seance.getHeureDebutIso();
                    String end = seance.getHeureFinIso();
                    String course = seance.getCours().getIntitule();
                    String room = seance.getSalle().getIdentifiantSalle();
                    String teacher = seance.getEnseignant().getNom();
                    int groupeId = seance.getGroupe().getId();
                    int enseignantId = seance.getEnseignant().getId();
                    int seanceId = seance.getId();
                    
                    sessionLines.add(new SessionLine(seanceId, day, start, end, course, room, teacher, groupeId, enseignantId));
                }
            } catch (Exception e) {
                // Si erreur, retourner liste vide
                System.err.println("Erreur chargement séances: " + e.getMessage());
            }
            return sessionLines;
        }

        private String getJourFrancais(String dayOfWeek) {
            return switch (dayOfWeek) {
                case "MONDAY" -> "Lundi";
                case "TUESDAY" -> "Mardi";
                case "WEDNESDAY" -> "Mercredi";
                case "THURSDAY" -> "Jeudi";
                case "FRIDAY" -> "Vendredi";
                case "SATURDAY" -> "Samedi";
                case "SUNDAY" -> "Dimanche";
                default -> dayOfWeek;
            };
        }

        private boolean isStudentMode() {
            return STUDENT_ROLE.equals(roleTitle);
        }

        private boolean isTeacherMode() {
            return TEACHER_ROLE.equals(roleTitle);
        }

        private boolean isAdminMode() {
            return ADMIN_ROLE.equals(roleTitle);
        }

        private Student currentStudent() {
            return currentStudent;
        }

        private Student resolveCurrentStudent() {
            if (!isStudentMode() || linkedEntityId == null) {
                return null;
            }
            return students.stream()
                .filter(student -> student.id() == linkedEntityId)
                .findFirst()
                .orElse(null);
        }

        private Teacher resolveCurrentTeacher() {
            if (!isTeacherMode() || linkedEntityId == null) {
                return null;
            }
            return teachers.stream()
                .filter(teacher -> teacher.id() == linkedEntityId)
                .findFirst()
                .orElse(null);
        }

        private List<NoteLine> filterNotesForStudent(List<NoteLine> allNotes, Student student) {
            return allNotes.stream()
                .filter(line -> line.studentProperty().get().equals(student.fullName()))
                .toList();
        }

        private List<String> filterEnrollmentsForStudent(List<String> enrollments, Student student) {
            String fullName = student.fullName().toLowerCase();
            return enrollments.stream()
                .filter(line -> line.toLowerCase().contains(fullName))
                .toList();
        }

        private List<SessionLine> filterSessionsForStudent(List<SessionLine> allSessions, Student student) {
            if (student == null) {
                return allSessions;
            }
            
            try {
                // Get student's inscriptions to find their groupe IDs
                Set<Integer> studentGroupeIds = new HashSet<>();
                models.Etudiant dbStudent = etudiantDao.findById(student.id()).orElse(null);
                
                if (dbStudent != null) {
                    // Get all inscriptions and filter by this student
                    List<models.Inscription> allInscriptions = inscriptionDao.findAll();
                    for (models.Inscription insc : allInscriptions) {
                        if (insc.getEtudiant().getId() == dbStudent.getId()) {
                            studentGroupeIds.add(insc.getGroupe().getId());
                        }
                    }
                }
                
                if (studentGroupeIds.isEmpty()) {
                    return List.of();
                }
                
                // Filter sessions by student's groupe IDs
                return allSessions.stream()
                    .filter(session -> studentGroupeIds.contains(session.groupeId))
                    .toList();
            } catch (Exception e) {
                System.err.println("Erreur filtrage sessions pour étudiant: " + e.getMessage());
                return allSessions;
            }
        }

        private List<String> filterNextSessionsForStudent(List<String> allNextSessions, List<NoteLine> studentNotes) {
            Set<String> ownCourses = new HashSet<>();
            for (NoteLine note : studentNotes) {
                ownCourses.add(note.courseProperty().get().toLowerCase());
            }
            if (ownCourses.isEmpty()) {
                return List.of();
            }
            return allNextSessions.stream()
                .filter(line -> ownCourses.stream().anyMatch(course -> line.toLowerCase().contains(course)))
                .toList();
        }

        private List<Course> filterCoursesForTeacher(List<Course> allCourses, Teacher teacher) {
            String teacherName = (teacher.lastName() + " " + teacher.firstName()).toLowerCase();
            String teacherLastName = teacher.lastName().toLowerCase();
            return allCourses.stream()
                .filter(course -> {
                    String owner = course.teacherProperty().get().toLowerCase();
                    return owner.contains(teacherName) || owner.contains(teacherLastName);
                })
                .toList();
        }

        private List<NoteLine> filterNotesForTeacher(List<NoteLine> allNotes, List<Course> teacherCourses) {
            Set<String> courseTitles = new HashSet<>();
            for (Course course : teacherCourses) {
                courseTitles.add(course.title().toLowerCase());
            }
            return allNotes.stream()
                .filter(note -> courseTitles.contains(note.courseProperty().get().toLowerCase()))
                .toList();
        }

        private List<Student> filterStudentsForTeacher(List<Student> allStudents, List<NoteLine> teacherNotes) {
            Set<String> studentNames = new HashSet<>();
            for (NoteLine note : teacherNotes) {
                studentNames.add(note.studentProperty().get().toLowerCase());
            }
            return allStudents.stream()
                .filter(student -> studentNames.contains(student.fullName().toLowerCase()))
                .toList();
        }

        private List<SessionLine> filterSessionsForTeacher(List<SessionLine> allSessions, Teacher teacher, List<Course> teacherCourses) {
            try {
                // Get the teacher's database ID
                models.Enseignant dbTeacher = enseignantDao.findById(teacher.id()).orElse(null);
                
                if (dbTeacher == null) {
                    // Fallback to old method if teacher not found in DB
                    Set<String> courseTitles = new HashSet<>();
                    for (Course course : teacherCourses) {
                        courseTitles.add(course.title().toLowerCase());
                    }
                    String teacherLastName = teacher.lastName().toLowerCase();
                    return allSessions.stream()
                        .filter(session -> {
                            String teacherValue = session.teacherProperty().get().toLowerCase();
                            String courseValue = session.courseProperty().get().toLowerCase();
                            return teacherValue.contains(teacherLastName)
                                || courseTitles.stream().anyMatch(courseValue::contains);
                        })
                        .toList();
                }
                
                final int teacherId = dbTeacher.getId();
                // Filter sessions by teacher's ID
                return allSessions.stream()
                    .filter(session -> session.enseignantId == teacherId)
                    .toList();
            } catch (Exception e) {
                System.err.println("Erreur filtrage sessions pour enseignant: " + e.getMessage());
                return allSessions;
            }
        }

        private List<String> filterEnrollmentsForTeacher(List<String> enrollments, List<Course> teacherCourses) {
            Set<String> courseTitles = new HashSet<>();
            for (Course course : teacherCourses) {
                courseTitles.add(course.title().toLowerCase());
            }
            return enrollments.stream()
                .filter(line -> {
                    String lowered = line.toLowerCase();
                    return courseTitles.stream().anyMatch(lowered::contains);
                })
                .toList();
        }

        private List<String> filterNextSessionsForTeacher(List<String> allNextSessions, List<Course> teacherCourses) {
            Set<String> courseTitles = new HashSet<>();
            for (Course course : teacherCourses) {
                courseTitles.add(course.title().toLowerCase());
            }
            return allNextSessions.stream()
                .filter(line -> {
                    String lowered = line.toLowerCase();
                    return courseTitles.stream().anyMatch(lowered::contains);
                })
                .toList();
        }

        private String userDisplayName() {
            if (isStudentMode() && currentStudent != null) {
                return currentStudent.fullName();
            }
            if (isTeacherMode() && currentTeacher != null) {
                return currentTeacher.firstName() + " " + currentTeacher.lastName();
            }
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
