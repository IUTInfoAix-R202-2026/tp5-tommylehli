package fr.univ_amu.iut.exercice7;

import com.google.inject.Inject;
import fr.univ_amu.iut.exercice4.Site;
import fr.univ_amu.iut.jdbc.DataAccessException;
import java.time.LocalDate;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Contrôleur de vue du capstone : affiche les sites persistés et permet d'en ajouter / supprimer.
 *
 * <p>Le contrôleur ne fait que câbler la vue au {@link SitesViewModel}. La suppression demande une
 * <b>confirmation</b> avant d'agir (prévention des erreurs, heuristique de Nielsen #5).
 */
public class SitesController {

  @Inject private SitesViewModel viewModel;

  @FXML private TableView<Site> tableSites;
  @FXML private TableColumn<Site, String> colNumero;
  @FXML private TableColumn<Site, String> colNom;
  @FXML private TableColumn<Site, String> colProtocole;
  @FXML private TextField champNumero;
  @FXML private TextField champNom;
  @FXML private ChoiceBox<String> choiceProtocole;
  @FXML private Button boutonAjouter;
  @FXML private Button boutonSupprimer;
  @FXML private Label labelResume;

  @FXML
  private void initialize() {
    // TODO exercice 7 : câbler la vue sur le ViewModel.
    //
    // 1. Pour chaque colonne, définir une cell value factory qui lit le champ du Site
    //    (numeroCarre, nomConvivial, protocole).
    // 2. Donner ses items à la TableView : setItems + viewModel.sitesProperty().
    // 3. Lier le texte de labelResume au resumeProperty() du ViewModel.
    // 4. Remplir choiceProtocole avec les deux protocoles ("PointFixeStandard",
    //    "PointFixeRecherche").
    // 5. Désactiver boutonSupprimer tant qu'aucune ligne n'est sélectionnée
    //    (disableProperty lié à selectedItemProperty().isNull() du selection model).

    colNumero.setCellValueFactory(
        c -> {
          return new SimpleStringProperty(c.getValue().numeroCarre());
        });

    colNom.setCellValueFactory(
        c -> {
          return new SimpleStringProperty(c.getValue().nomConvivial());
        });

    colProtocole.setCellValueFactory(
        c -> {
          return new SimpleStringProperty(c.getValue().protocole());
        });

    tableSites.setItems(viewModel.sitesProperty());
    labelResume.textProperty().bind(viewModel.resumeProperty());
    choiceProtocole.getItems().addAll("PointFixeStandard", "PointFixeRecherche");

    boutonSupprimer
        .disableProperty()
        .bind(tableSites.getSelectionModel().selectedItemProperty().isNull());
  }

  @FXML
  private void surAjouter() {
    String numero = champNumero.getText();
    String nom = champNom.getText();
    String protocole = choiceProtocole.getValue();
    if (numero == null || numero.isBlank() || protocole == null) {
      return; // affordance : on n'ajoute pas un site incomplet
    }
    viewModel.ajouterCommand(new Site(numero, nom, protocole, null, LocalDate.now().toString()));
    champNumero.clear();
    champNom.clear();
  }

  @FXML
  private void surSupprimer() {
    Site selectionne = tableSites.getSelectionModel().getSelectedItem();
    if (selectionne == null) {
      return;
    }
    Alert confirmation =
        new Alert(
            AlertType.CONFIRMATION,
            "Supprimer définitivement le site " + selectionne.numeroCarre() + " ?");
    Optional<ButtonType> reponse = confirmation.showAndWait();
    if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
      try {
        viewModel.supprimerCommand(selectionne);
      } catch (DataAccessException e) {
        // Cas typique : le site a des points d'écoute / passages rattachés
        // (intégrité référentielle). On le dit en langage humain (Nielsen #9).
        new Alert(
                AlertType.ERROR,
                "Impossible de supprimer ce site : il a des données rattachées"
                    + " (points d'écoute, passages...).")
            .showAndWait();
      }
    }
  }
}
