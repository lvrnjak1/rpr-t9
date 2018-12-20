package ba.unsa.etf.rpr;

import java.sql.Connection;

public class GeografijaDAO {
    private static GeografijaDAO instance = null;
    private Connection con;

    private static void initialize(){
        instance = new GeografijaDAO();
    }

    private GeografijaDAO(){
        //postavlja konekciju
        //Class.forName("");
        //con = DriverManager.getconnection(...);

        //pripremiti upite

        //ako datoteka baza.db ne postoji napuniti podacima za
        //Pariz(2.2 mil), London(8.136 mil), Bec(1.868 mil.), Manchester(510 746), Graz(283 869)
        //te njihove pripadajuce drzave Francuska, Engleska, Austrija

    }

    public static GeografijaDAO getInstance(){
        if(instance == null) initialize();
        return instance;
    }

    public static void removeInstance(){
        instance = null;
    }
}
