package ba.unsa.etf.rpr;

import java.sql.*;
import java.util.ArrayList;

public class GeografijaDAO {
    private static GeografijaDAO instance = null;
    private Connection con;
    //private String url;
    private Statement statement;
    private PreparedStatement procitajGlGrad, procitajDrzavu;

    private static void initialize() throws SQLException {
        instance = new GeografijaDAO();
    }

    private GeografijaDAO() throws SQLException {
        //postavlja konekciju
        //Class.forName("");
        //con = DriverManager.getconnection(...);

        //pripremiti upite
        procitajGlGrad = con.prepareStatement("SELECT g.naziv, g.broj_stanovnika" +
                "FROM grad g, drzava d" +
                "WHERE g.id = d.glavni_grad AND d.naziv = ?");

        procitajDrzavu = con.prepareStatement("SELECT id " +
                                                   "FROM drzava" +
                                                   "WHERE naziv = ?");


        //ako datoteka baza.db ne postoji napuniti podacima za
        //Pariz(2.2 mil), London(8.136 mil), Bec(1.868 mil.), Manchester(510 746), Graz(283 869)
        //te njihove pripadajuce drzave Francuska, Engleska, Austrija

    }

    public static GeografijaDAO getInstance() throws SQLException {
        if(instance == null) initialize();
        return instance;
    }

    public static void removeInstance(){
        instance = null;
    }

    public Grad glavniGrad(String drzava) throws SQLException {
        procitajGlGrad.setString(1, drzava);
        ResultSet result = procitajGlGrad.executeQuery();
        if(!result.next()) return null;

        Grad procitaniGrad = new Grad(result.getString(1), result.getInt(2), new Drzava(drzava, null));
        procitaniGrad.getDrzava().setGlavniGrad(procitaniGrad);

        return procitaniGrad;
    }

    public void obrisiDrzavu(String drzava) throws SQLException {
        procitajDrzavu.setString(1,drzava);
        ResultSet result = procitajDrzavu.executeQuery();
        if(!result.next()) return;

        statement = con.createStatement();
        statement.executeUpdate("DELETE FROM grad WHERE drzava = " + result.getInt(1));
        statement.executeUpdate("DELETE FROM drzava WHERE id = " + result.getInt(1));
    }
}
