package org.example.views.components.dialogs;

import java.util.List;

import com.gaming.api.models.DLCModel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DLCListDialog {

    public static void show(String gameName, List<DLCModel> dlcs) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("DLCs — " + gameName);

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setStyle("-fx-background-color: #2b2b2b;");

        Label title = new Label(gameName + " — " + dlcs.size() + " DLC(s)");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox list = new VBox(8);
        if (dlcs == null || dlcs.isEmpty()) {
            Label none = new Label("Aucun DLC disponible pour ce jeu.");
            none.setStyle("-fx-text-fill: #ccc;");
            list.getChildren().add(none);
        } else {
            for (DLCModel d : dlcs) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                Label name = new Label(d.getTitle());
                name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                Label meta = new Label(d.getDescription() != null ? d.getDescription() : "");
                meta.setStyle("-fx-text-fill: #aaa;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Button open = new Button("Fermer");
                open.setOnAction(e -> dialog.close());
                row.getChildren().addAll(name, meta, spacer, open);
                list.getChildren().add(row);
                list.getChildren().add(new Separator());
            }
        }

        Button close = new Button("Fermer");
        close.setOnAction(e -> dialog.close());
        HBox footer = new HBox(close);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(title, new Separator(), list, new Separator(), footer);

        Scene scene = new Scene(root, 520, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
