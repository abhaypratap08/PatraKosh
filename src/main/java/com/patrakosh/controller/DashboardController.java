package com.patrakosh.controller;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.patrakosh.MainApp;
import com.patrakosh.model.FileItem;
import com.patrakosh.model.User;
import com.patrakosh.service.FileService;
import com.patrakosh.util.FileUtil;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class DashboardController {
    
    @FXML private Label welcomeLabel;
    @FXML private MenuButton userMenuButton;
    @FXML private MenuItem logoutMenuItem;
    @FXML private Button uploadButton;
    @FXML private Button downloadButton;
    @FXML private Button deleteButton;
    @FXML private Button renameButton;
    @FXML private Button refreshButton;
    @FXML private TextField searchField;
    @FXML private Label totalFilesLabel;
    @FXML private Label storageUsedLabel;
    @FXML private TableView<FileItem> filesTable;
    @FXML private TableColumn<FileItem, String> iconColumn;
    @FXML private TableColumn<FileItem, String> filenameColumn;
    @FXML private TableColumn<FileItem, String> sizeColumn;
    @FXML private TableColumn<FileItem, String> uploadDateColumn;
    @FXML private TableColumn<FileItem, Void> actionsColumn;
    @FXML private Label statusLabel;
    
    private FileService fileService;
    private User currentUser;
    private ObservableList<FileItem> filesList;
    
    @FXML
    public void initialize() {
        fileService = new FileService();
        currentUser = MainApp.getCurrentUser();
        filesList = FXCollections.observableArrayList();
        
        // Set welcome message
        welcomeLabel.setText("Welcome, " + currentUser.getUsername());
        
        // Setup table columns
        setupTableColumns();
        
        // Load files
        loadFiles();
        
        // Update stats
        updateStats();
    }
    
    private void setupTableColumns() {
        // Icon column
        iconColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(FileUtil.getFileIcon(cellData.getValue().getFilename()))
        );
        iconColumn.setStyle("-fx-alignment: CENTER; -fx-font-size: 20px;");
        
        // Filename column
        filenameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        
        // Size column
        sizeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(FileUtil.formatFileSize(cellData.getValue().getFileSize()))
        );
        sizeColumn.setStyle("-fx-alignment: CENTER;");
        
        // Upload date column
        uploadDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(FileUtil.formatTimeAgo(cellData.getValue().getUploadTime()))
        );
        uploadDateColumn.setStyle("-fx-alignment: CENTER;");
        
        // Actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button downloadBtn = new Button("⬇");
            private final Button deleteBtn = new Button("🗑");
            private final HBox pane = new HBox(10, downloadBtn, deleteBtn);
            
            {
                pane.setAlignment(Pos.CENTER);
                
                downloadBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                                   "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5 10; -fx-font-size: 14px;");
                downloadBtn.setOnAction(event -> {
                    FileItem file = getTableView().getItems().get(getIndex());
                    handleDownload(file);
                });
                
                deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; " +
                                 "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5 10; -fx-font-size: 14px;");
                deleteBtn.setOnAction(event -> {
                    FileItem file = getTableView().getItems().get(getIndex());
                    handleDelete(file);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        filesTable.setItems(filesList);
        
        // Style table rows with proper selection handling
        filesTable.setRowFactory(tv -> {
            TableRow<FileItem> row = new TableRow<>() {
                @Override
                protected void updateItem(FileItem item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setStyle("");
                    } else {
                        // Normal state
                        if (!isSelected()) {
                            setStyle("-fx-background-color: white; -fx-text-fill: black;");
                        } else {
                            // Selected state - visible colors
                            setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                        }
                    }
                }
            };
            
            // Hover effect
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty() && !row.isSelected()) {
                    row.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: black;");
                }
            });
            
            row.setOnMouseExited(event -> {
                if (!row.isEmpty() && !row.isSelected()) {
                    row.setStyle("-fx-background-color: white; -fx-text-fill: black;");
                }
            });
            
            // Update style when selection changes
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (!row.isEmpty()) {
                    if (isNowSelected) {
                        row.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                    } else {
                        row.setStyle("-fx-background-color: white; -fx-text-fill: black;");
                    }
                }
            });
            
            return row;
        });
    }
    
    private void loadFiles() {
        new Thread(() -> {
            try {
                List<FileItem> files = fileService.getUserFiles(currentUser.getId());
                
                Platform.runLater(() -> {
                    filesList.clear();
                    filesList.addAll(files);
                    updateStatus("Files loaded successfully");
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to load files: " + e.getMessage());
                    updateStatus("Error loading files");
                });
            }
        }).start();
    }
    
    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Upload");
        
        Stage stage = (Stage) uploadButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            uploadButton.setDisable(true);
            uploadButton.setText("Uploading...");
            updateStatus("Uploading file...");
            
            new Thread(() -> {
                try {
                    fileService.uploadFile(currentUser.getId(), selectedFile);
                    
                    Platform.runLater(() -> {
                        uploadButton.setDisable(false);
                        uploadButton.setText("⬆ Upload File");
                        showAlert(Alert.AlertType.INFORMATION, "Success", "File uploaded successfully!");
                        loadFiles();
                        updateStats();
                        updateStatus("File uploaded successfully");
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        uploadButton.setDisable(false);
                        uploadButton.setText("⬆ Upload File");
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload file: " + e.getMessage());
                        updateStatus("Upload failed");
                    });
                }
            }).start();
        }
    }
    
    private void handleDownload(FileItem file) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialFileName(file.getFilename());
        
        Stage stage = (Stage) filesTable.getScene().getWindow();
        File destinationFile = fileChooser.showSaveDialog(stage);
        
        if (destinationFile != null) {
            updateStatus("Downloading file...");
            
            new Thread(() -> {
                try {
                    fileService.downloadFile(file, destinationFile);
                    
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "File downloaded successfully!");
                        updateStatus("File downloaded successfully");
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to download file: " + e.getMessage());
                        updateStatus("Download failed");
                    });
                }
            }).start();
        }
    }
    
    private void handleDelete(FileItem file) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete File");
        confirmAlert.setContentText("Are you sure you want to delete \"" + file.getFilename() + "\"?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            updateStatus("Deleting file...");
            
            new Thread(() -> {
                try {
                    fileService.deleteFile(file.getId());
                    
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "File deleted successfully!");
                        loadFiles();
                        updateStats();
                        updateStatus("File deleted successfully");
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete file: " + e.getMessage());
                        updateStatus("Delete failed");
                    });
                }
            }).start();
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadFiles();
        updateStats();
        updateStatus("Refreshed");
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        
        new Thread(() -> {
            try {
                List<FileItem> files = fileService.searchFiles(currentUser.getId(), searchTerm);
                
                Platform.runLater(() -> {
                    filesList.clear();
                    filesList.addAll(files);
                    updateStatus("Search completed");
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Search failed: " + e.getMessage());
                    updateStatus("Search failed");
                });
            }
        }).start();
    }
    
    @FXML
    private void handleDownloadSelected() {
        FileItem selectedFile = filesTable.getSelectionModel().getSelectedItem();
        
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a file to download.");
            return;
        }
        
        handleDownload(selectedFile);
    }
    
    @FXML
    private void handleDeleteSelected() {
        FileItem selectedFile = filesTable.getSelectionModel().getSelectedItem();
        
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a file to delete.");
            return;
        }
        
        handleDelete(selectedFile);
    }
    
    @FXML
    private void handleRenameSelected() {
        FileItem selectedFile = filesTable.getSelectionModel().getSelectedItem();
        
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a file to rename.");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(selectedFile.getFilename());
        dialog.setTitle("Rename File");
        dialog.setHeaderText("Rename File");
        dialog.setContentText("Enter new filename:");
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(newFilename -> {
            if (newFilename.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Filename cannot be empty.");
                return;
            }
            
            updateStatus("Renaming file...");
            
            new Thread(() -> {
                try {
                    // Update filename in database
                    selectedFile.setFilename(newFilename.trim());
                    fileService.updateFile(selectedFile);
                    
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "File renamed successfully!");
                        loadFiles();
                        updateStatus("File renamed successfully");
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to rename file: " + e.getMessage());
                        updateStatus("Rename failed");
                    });
                }
            }).start();
        });
    }
    
    @FXML
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Logout");
        confirmAlert.setHeaderText("Logout");
        confirmAlert.setContentText("Are you sure you want to logout?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            MainApp.setCurrentUser(null);
            MainApp.showLogin();
        }
    }
    
    private void updateStats() {
        new Thread(() -> {
            try {
                int fileCount = fileService.getFileCount(currentUser.getId());
                long storageUsed = fileService.getTotalStorageUsed(currentUser.getId());
                
                Platform.runLater(() -> {
                    totalFilesLabel.setText(String.valueOf(fileCount));
                    storageUsedLabel.setText(FileUtil.formatFileSize(storageUsed));
                });
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
