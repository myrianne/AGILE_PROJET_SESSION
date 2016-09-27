/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testprojectagile;

/**
 *
 * @author ShadDaBeast
 */
import java.io.*;
import java.util.ArrayList;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import java.text.*;
import java.util.Date;

public class TestProjectAgile {
    static int HeuresMin17=0;
    static int HeuresTotales = 0;
    static int HeuresPresentation=0;
    static int HeuresDiscussion=0;
    static int HeuresRecherche=0;
    static int HeuresRedaction=0;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
        int condition=0;
        String [] listeConditions1={"cours,atelier, séminaire, colloque"};
        
        JSONArray erreurs = new JSONArray(); // array erreurs qui apparaitera 
        //dans le JSON de sortie
        String JSONTxt = null;
        try {
            JSONTxt = Utf8File.loadFileIntoString("ActivitesFormation.json");

        } catch (IOException ex) {
            System.out.println("Erreur " + ex.getLocalizedMessage());
        }
        JSONObject racine;
        racine = (JSONObject) JSONSerializer.toJSON(JSONTxt);

        if (!validerCycle(racine, "2014-2016")) {
            erreurs.add("Le cycle déclaré ne correspond pas au cycle considéré");

        } else {
            int heuresCyclePrecedent;
            heuresCyclePrecedent = racine.getInt("heures_transferees_du_cycle_precedent");
            if (heuresCyclePrecedent < 0) {
                erreurs.add("Le nombre d'heures transférées du cycle "
                        + "précédent ne peut pas être négatif");
            }
            if (heuresCyclePrecedent > 7) {

                erreurs.add("Sur vos " + Integer.toString(heuresCyclePrecedent)
                        + " heures en plus du cycle précédent"
                        + " seulement un maximum de 7 heures peut être considéré dans le calcul");
                
                heuresCyclePrecedent=7;
            }
            HeuresTotales += heuresCyclePrecedent;
            HeuresMin17 += heuresCyclePrecedent;
            JSONArray activites = racine.getJSONArray("activites");
            JSONObject item = null;
            
            for (int i = 0; i < activites.size(); ++i) {
                item = activites.getJSONObject(i);
                String categorie = item.getString("catégorie");
                String date= item.getString("date");
                int heures = item.getInt("heures");
                
                
                if (!validerActivite(categorie)) {
                    erreurs.add("L'activité " + item.getString("description")
                            + " est dans une catégorie non reconnue. Elle sera ignorée.");

                }else if (!validerDate( date)) {
                    erreurs.add("L'activité "+categorie 
                            + " n'a pas été complétée entre le 1er Avril 2014 "
                            + "et le 1er Avril 2016");
                }else{
                  
                    switch (categorie) {
                        case "cours": condition=1;
                        break;
                        
                        case "atelier": condition=1;
                        break;
          
                        case "séminaire": condition=1;
                        break;
                        
                        case "colloque": condition=1;
                        break;
                        
                        case "conférence": condition=1;
                        break;
                        
                        case "lecture dirigée": condition=1;
                        break;
                        
                        case "présentation": condition=2;
                        break;
                        
                        case "groupe de discussion": condition=3;
                        break;    
          
                        case "projet de recherche": condition=4;
                        break;    
                
                        case "rédaction professionnelle": condition=5;
                        break;
                        
                        default: System.out.println("Cette catégorie ne figure pas parmis celles exigibles.");
                        break;   
                    }
                    switch(condition){
                
                        case 1: //
                            validerMin1hre(heures);
                            ajouterMin17(heures);
                            ajouterMin40(heures);
                            break;
                        
                        case 2: //présentation
                            validerMin1hre(heures);
                            verifierHrPresentation(heures);
                            ajouterMin40(heures);
                            break;
                        
                        case 3: //discussion
                            validerMin1hre(heures);
                            verifierHrDiscussion(heures);
                            ajouterMin40(heures);
                            break;
                        
                        case 4: //recherche
                            validerMin1hre(heures);
                            verifierHrRecherche(heures);
                            ajouterMin40(heures);
                            break;
                        
                        case 5: //rédaction
                            validerMin1hre(heures);
                            verifierHrRedaction(heures);
                            ajouterMin40(heures);
                            break;
                           
                    }
                }
                
            }
        
        if(!validerMin17()){
            System.out.println("Le minimum de 17 heures requis pour l'ensemble " 
                    +" des catégories suivantes n'a pas été atteint: cours, atelier," 
                    +" séminaire, colloque, conférence et lecture dirigée.");
        }
        
        if(!validerMin40()){
            System.out.println("Le minimum de 40 heures requis pour l'ensemble" 
                    + " des catégorie n'a pas été atteint");
        }
        
        }
    }


    public static boolean validerCycle(JSONObject racine, String cycle) {
        boolean valide = false;
        if (cycle.equals(racine.get("cycle"))) {
            valide = true;
        }
        return valide;
    }

    public static boolean validerActivite(String description) {
        boolean valide = false;
        String[] categories = {"cours", "atelier", "séminaire", "colloque",
            "conférence", "lecture dirigée", "présentation",
            "groupe de discussion", "projet de recherche",
            "rédaction professionnelle"};
        for (String s : categories) {
            if (description.equals(s)) {
                valide = true;
            }
        }
        return valide;
    }
    public static boolean validerDate( String date){
      boolean valide = false;  
      SimpleDateFormat isoFormat= new SimpleDateFormat ("yyyy-MM-dd"); 
      Date dateInf= null;
      Date dateSup= null;
      Date dateActivite= null;
      try {
          dateInf= isoFormat.parse("2014-04-01");
          dateSup= isoFormat.parse("2016-04-01");
          dateActivite= isoFormat.parse(date);
      }catch (ParseException p) {System.out.println("Erreur d'analyse");
          
      }
      if (dateActivite.compareTo(dateInf) >= 0  
              && dateActivite.compareTo(dateSup) <= 0){
          valide= true;
      }
         return valide; 
    }
    
    public static boolean validerMin1hre(int heure){
        boolean estValide=false;
        
        if(heure<1){
            estValide=true;
        }
        return estValide;
    }
    public static void ajouterMin17(int heure){
        HeuresMin17 += heure;
    }
    public static void ajouterMin40(int heure){
        HeuresTotales += heure;
    }
    public static void verifierHrPresentation(int heure){
        if(heure>23){
            heure=23;
        }
        HeuresPresentation += heure;
    }
    public static void verifierHrDiscussion(int heure){
        if(heure>17){
            heure=17;
        }
        HeuresDiscussion += heure;
    }
    public static void verifierHrRecherche(int heure){
        if(heure>23){
            heure=23;
        }
        HeuresRecherche += heure;
    }
    public static void verifierHrRedaction(int heure){
        if(heure>17){
            heure=17;
        }
        HeuresRedaction += heure;
    }
    public static boolean validerMin40(){
        boolean estValide=true;
        
        if(HeuresTotales<40){
            estValide=false;
        }
        return estValide;
    }
    public static boolean validerMin17(){
        boolean estValide=true;
        
        if(HeuresMin17<17){
            estValide=false;
        }
        return estValide;
    }
}
