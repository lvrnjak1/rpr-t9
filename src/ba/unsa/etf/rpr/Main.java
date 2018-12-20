package ba.unsa.etf.rpr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws SQLException {
        System.out.println("Gradovi su:\n" + ispisiGradove());
        glavniGrad();
    }

    private static String ispisiGradove() throws SQLException {
            GeografijaDAO g = GeografijaDAO.getInstance();
            ArrayList<Grad> gradovi = g.gradovi();
            String ispis = "";
            for(Grad gr : gradovi)
                ispis += gr;

            GeografijaDAO.removeInstance();
            return ispis;
    }

    private static void glavniGrad() throws SQLException {
        GeografijaDAO g = GeografijaDAO.getInstance();

        System.out.println("Unesite naziv drzave\n");
        Scanner unos = new Scanner(System.in);
        String drzava = unos.nextLine();
        Grad glGrad = g.glavniGrad(drzava);
        if(glGrad == null)
            System.out.println("Nepostojeća država");
        System.out.println("Glavni grad države " + drzava + " je " + glGrad.getNaziv());
    }
}
