package com.patrakosh.controller;

import com.patrakosh.MainApp;
import com.patrakosh.model.User;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class DashboardController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final ObservableList<DesktopFileRow> files = FXCollections.observableArrayList();
    private Path desktopStorageRoot;
    private FilteredList<DesktopFileRow> filteredFiles;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<DesktopFileRow> filesTable;

    @FXML
    private TableColumn<DesktopFileRow, String> iconColumn;

    @FXML
    private TableColumn<DesktopFileRow, String> filenameColumn;

    @FXML
    private TableColumn<DesktopFileRow, String> sizeColumn;

    @FXML
    private TableColumn<DesktopFileRow, String> uploadDateColumn;

    @FXML
    private TableColumn<DesktopFileRow, String> actionsColumn;

    @FXML
    private void initialize() {
        User user = MainApp.getCurrentUser();
        if (user == null) {
            MainApp.showLogin();
            return;
        }

        desktopStorageRoot = MainApp.getDesktopStorageBasePath().resolve("user-" + user.getId());
        configureTable();
        filteredFiles = new FilteredList<>(files, ignored -> true);
        filesTable.setItems(filteredFiles);
        loadExistingFiles();

        welcomeLabel.setText("Welcome, " + user.getUsername());
        updateStatus("Loaded " + filteredFiles.size() + " files.");
    }

    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file to upload");
        File selected = fileChooser.showOpenDialog(filesTable.getScene().getWindow());
        if (selected == null) {
            return;
        }

        try {
            Files.createDirectories(desktopStorageRoot);
            Path target = uniqueTargetPath(selected.getName());
            Files.copy(selected.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            files.add(DesktopFileRow.fromPath(target));
            sortFiles();
            updateFilter();
            updateStatus("Uploaded " + selected.getName() + ".");
        } catch (IOException e) {
            showError("Upload failed", "Could not import the selected file.");
        }
    }

    @FXML
    private void handleDownloadSelected() {
        DesktopFileRow selected = selectedRow();
        if (selected == null) {
            showError("No file selected", "Choose a file before downloading.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        fileChooser.setInitialFileName(selected.filename());
        File destination = fileChooser.showSaveDialog(filesTable.getScene().getWindow());
        if (destination == null) {
            return;
        }

        try {
            Files.copy(selected.storedPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            updateStatus("Downloaded " + selected.filename() + ".");
        } catch (IOException e) {
            showError("Download failed", "Could not save the selected file.");
        }
    }

    @FXML
    private void handleDeleteSelected() {
        DesktopFileRow selected = selectedRow();
        if (selected == null) {
            showError("No file selected", "Choose a file before deleting.");
            return;
        }

        try {
            Files.deleteIfExists(selected.storedPath());
            files.remove(selected);
            updateFilter();
            updateStatus("Deleted " + selected.filename() + ".");
        } catch (IOException e) {
            showError("Delete failed", "Could not remove the selected file.");
        }
    }

    @FXML
    private void handleRenameSelected() {
        DesktopFileRow selected = selectedRow();
        if (selected == null) {
            showError("No file selected", "Choose a file before renaming.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selected.filename());
        dialog.setTitle("Rename file");
        dialog.setHeaderText(null);
        dialog.setContentText("New file name:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String nextFilename = sanitizeFilename(result.get());
        if (nextFilename.isBlank()) {
            showError("Rename failed", "Enter a valid file name.");
            return;
        }
        if (nextFilename.equals(selected.filename())) {
            updateStatus("Filename unchanged.");
            return;
        }

        try {
            Path target = uniqueTargetPath(nextFilename);
            Files.move(selected.storedPath(), target, StandardCopyOption.REPLACE_EXISTING);

            int rowIndex = files.indexOf(selected);
            DesktopFileRow updated = DesktopFileRow.fromPath(target);
            files.set(rowIndex, updated);
            filesTable.getSelectionModel().select(updated);
            updateFilter();
            updateStatus("Renamed file to " + nextFilename + ".");
        } catch (IOException e) {
            showError("Rename failed", "Could not rename the selected file.");
        }
    }

    @FXML
    private void handleSearch() {
        updateFilter();
    }

    @FXML
    private void handleLogout() {
        MainApp.setCurrentUser(null);
        MainApp.showLogin();
    }

    private void configureTable() {
        iconColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(iconFor(cell.getValue().filename())));
        filenameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().filename()));
        sizeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatBytes(cell.getValue().fileSize())));
        uploadDateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(DATE_FORMAT.format(cell.getValue().uploadedAt())));
        actionsColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper("Use toolbar"));
    }

    private void loadExistingFiles() {
        try {
            Files.createDirectories(desktopStorageRoot);
            try (Stream<Path> stream = Files.list(desktopStorageRoot)) {
                stream.filter(Files::isRegularFile)
                        .map(DesktopFileRow::fromPathUnchecked)
                        .sorted(Comparator.comparing(DesktopFileRow::uploadedAt).reversed())
                        .forEach(files::add);
            }
        } catch (IOException e) {
            showError("Storage unavailable", "Could not access the local desktop storage directory.");
        }
    }

    private void updateFilter() {
        String query = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();
        filteredFiles.setPredicate(row -> query.isBlank() || row.filename().toLowerCase().contains(query));
        sortFiles();
        updateStatus("Showing " + filteredFiles.size() + " files.");
    }

    private void sortFiles() {
        FXCollections.sort(files, Comparator.comparing(DesktopFileRow::uploadedAt).reversed());
    }

    private DesktopFileRow selectedRow() {
        return filesTable.getSelectionModel().getSelectedItem();
    }

    private Path uniqueTargetPath(String originalFilename) {
        String safeFilename = sanitizeFilename(originalFilename);
        if (safeFilename.isBlank()) {
            safeFilename = "file";
        }

        Path candidate = desktopStorageRoot.resolve(safeFilename);
        if (!Files.exists(candidate)) {
            return candidate;
        }

        int dotIndex = safeFilename.lastIndexOf('.');
        String basename = dotIndex > 0 ? safeFilename.substring(0, dotIndex) : safeFilename;
        String extension = dotIndex > 0 ? safeFilename.substring(dotIndex) : "";

        int counter = 1;
        while (true) {
            Path next = desktopStorageRoot.resolve(basename + " (" + counter + ")" + extension);
            if (!Files.exists(next)) {
                return next;
            }
            counter++;
        }
    }

    private static String sanitizeFilename(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');
        String filename = slashIndex >= 0 ? normalized.substring(slashIndex + 1) : normalized;
        return filename.replaceAll("[\\r\\n]+", "_");
    }

    private void updateStatus(String text) {
        statusLabel.setText(text);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        updateStatus(message);
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        double value = bytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int unitIndex = -1;
        while (value >= 1024 && unitIndex < units.length - 1) {
            value /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", value, units[unitIndex]);
    }

    private static String iconFor(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".gif")) {
            return "🖼";
        }
        if (lower.endsWith(".pdf")) {
            return "📄";
        }
        if (lower.endsWith(".zip") || lower.endsWith(".rar")) {
            return "🗜";
        }
        if (lower.endsWith(".mp4") || lower.endsWith(".mov")) {
            return "🎞";
        }
        return "📦";
    }

    private record DesktopFileRow(String filename, long fileSize, LocalDateTime uploadedAt, Path storedPath) {

        private static DesktopFileRow fromPath(Path path) throws IOException {
            FileTime lastModified = Files.getLastModifiedTime(path);
            return new DesktopFileRow(
                    path.getFileName().toString(),
                    Files.size(path),
                    LocalDateTime.ofInstant(lastModified.toInstant(), ZoneId.systemDefault()),
                    path
            );
        }

        private static DesktopFileRow fromPathUnchecked(Path path) {
            try {
                return fromPath(path);
            } catch (IOException e) {
                return new DesktopFileRow(
                        path.getFileName().toString(),
                        0L,
                        LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault()),
                        path
                );
            }
        }
    }
}
