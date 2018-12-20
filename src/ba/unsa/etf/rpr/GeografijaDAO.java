package ba.unsa.etf.rpr;

import java.sql.*;
import java.util.ArrayList;

public class GeografijaDAO {
    private static GeografijaDAO instance = null;
    private Connection con;
    //private String url;
    private Statement statement;
    private PreparedStatement procitajGlGrad, procitajDrzavu, procitajSveGradove;

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

        procitajSveGradove = con.prepareStatement("SELECT g.naziv, g.broj_stanovnika, g.drzava " +
                                                       "FROM grad g" +
                                                       "ORDER BY g.broj_stanovnika DESC");

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

    public ArrayList<Grad> gradovi() throws SQLException {
        //cita sve gradove iz pripremljenog upita u desc poretku po broju stanovnika
        ResultSet result = procitajSveGradove.executeQuery();
        if(!result.next()) return null;

        ArrayList<Grad> gradovi = new ArrayList<>();

        //prolazi kroz procitane gradove
        //1-naziv, 2-broj_stanovnika, 3-id drzave
        while (result.next()){
            statement = con.createStatement();
            //ovim upitom dobijem naziv drzave u kojoj se nalazi grad kojeg trenutno obradjujem
            ResultSet d = statement.executeQuery("SELECT naziv" +
                                                      "FROM drzava" +
                                                      "WHERE id = " + result.getInt(3));

            //pozovem metodu glavni grad da dobijem gl.grad od drzave koju upit iznad vrati
            //zatim to + ostali podaci idu u konstruktor grada
            Grad zaDodati = new Grad(result.getString(1), result.getInt(2),
                    new Drzava(d.getString(1), glavniGrad(d.getString(1))));

            //stavlja se u array listu
            gradovi.add(zaDodati);
       }

       return gradovi;
    }

    public void dodajGrad(Grad grad) throws SQLException {
        //provjeriti da li vec postoji
        statement = con.createStatement();
        ResultSet postoji = statement.executeQuery("SELECT id FROM grad WHERE naziv = " + grad.getNaziv());
        if(postoji.next()) return;

        PreparedStatement upit = con.prepareStatement("INSERT INTO grad (naziv, broj_stanovnika, drzava) VALUES (?, ?, ?)");
        upit.setString(1,grad.getNaziv());
        upit.setInt(2,grad.getBrStanovnika());

        //statement = con.createStatement();
        //ovim upitom dobijem id drzave u kojoj se grad iz parametra metode nalazi
        ResultSet drzavaId = statement.executeQuery("SELECT id FROM drzava WHERE naziv = " + grad.getDrzava().getNaziv());

        //ako nema te drzave
        //treba dodati nju i njen glavni grad
        //nadajmo se da to radi ova metoda
        if(!drzavaId.next()) {
            dodajDrzavu(grad.getDrzava());
            //uzmi taj dodani id
            ResultSet drzavaId1 = statement.executeQuery("SELECT id FROM drzava WHERE naziv = " + grad.getDrzava().getNaziv());
            upit.setInt(3, drzavaId1.getInt(1));
        }
        else
            upit.setInt(3, drzavaId.getInt(1));

        upit.executeUpdate();
    }

    private void dodajDrzavu(Drzava drzava) throws SQLException {
        //provjeriti da li vec postoji
        statement = con.createStatement();
        ResultSet postoji = statement.executeQuery("SELECT id FROM drzava WHERE naziv = " + drzava.getNaziv());
        if (postoji.next()) return;

        PreparedStatement upit = con.prepareStatement("INSERT INTO drzava (naziv, glavni_grad) VALUES (?, ?)");
        upit.setString(1, drzava.getNaziv());

        //dobijem id glavnog grada drzava iz parametra metode
        ResultSet gradId = statement.executeQuery("SELECT id FROM grad WHERE naziv = " + drzava.getGlavniGrad().getNaziv());

        //ako taj gl grad ne postoji
        if(!gradId.next()) {
            upit.setNull(2, Types.INTEGER);
            //ovime se izbjegne da ne dodje do beskonacnog pozivanja dodaj grad dodaj drzavu
        }
        else
            upit.setInt(2, gradId.getInt(1));

        upit.executeUpdate();

        //sad je drzava dodana pa mozemo dodati grad
        dodajGrad(drzava.getGlavniGrad());
        //uzmi id dodanog grada
        ResultSet grad = statement.executeQuery("SELECT id FROM grad WHERE naziv = " + drzava.getGlavniGrad().getNaziv());


        statement.executeUpdate("UPDATE drzava SET glavni_grad = " + grad.getInt(1) + "WHERE naziv = " + drzava.getNaziv());
    }

    public Drzava nadjiDrzavu(String drzava) throws SQLException {
        procitajDrzavu.setString(1,drzava);
        ResultSet drzavaId = procitajDrzavu.executeQuery();

        if(!drzavaId.next()) return null;

        Drzava d = new Drzava(drzava, glavniGrad(drzava));

        return d;
    }
}
