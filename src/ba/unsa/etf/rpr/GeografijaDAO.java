package ba.unsa.etf.rpr;

import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.ArrayList;

public class GeografijaDAO {
    //TODO
    //postaviti konekciju u konstruktoru
    //napuniti bazu podacima

    private static GeografijaDAO instance;

    static {
        try {
            instance = new GeografijaDAO();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection con;
    private Statement statement;
    private PreparedStatement procitajGlGrad, procitajDrzavu, procitajSveGradove;

    /*private static void initialize() throws SQLException {
        instance = new GeografijaDAO();
    }*/
    
    private GeografijaDAO() throws SQLException {
        //postavlja konekciju
        //Class.forName("com.mysql.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:sqlite:baza.db");



        //ako datoteka baza.db ne postoji napuniti podacima za
        //Pariz(2.2 mil), London(8.136 mil), Bec(1.868 mil.), Manchester(510 746), Graz(283 869)
        //te njihove pripadajuce drzave Francuska, Engleska, Austrija
        try {
            Statement st = con.createStatement();
            st.executeQuery("select * from grad");

        }catch (Exception e){
            popuniTabele();
        }

        //pripremiti upite
        procitajGlGrad = con.prepareStatement("SELECT g.naziv, g.broj_stanovnika " +
                "FROM grad g, drzava d " +
                "WHERE g.id = d.glavni_grad AND d.naziv = ?;");

        procitajDrzavu = con.prepareStatement("SELECT id " +
                "FROM drzava " +
                "WHERE naziv = ?;");

        procitajSveGradove = con.prepareStatement("SELECT g.naziv, g.broj_stanovnika, g.drzava " +
                "FROM grad g " +
                "ORDER BY g.broj_stanovnika DESC;");

    }

    private void popuniTabele() throws SQLException {
        Statement napraviTabelu = con.createStatement();
        napraviTabelu.execute("create table grad ( id integer primary key, naziv varchar(50) not null, broj_stanovnika integer);");
        napraviTabelu.execute("create table drzava (id integer primary key, naziv varchar(50) not null);");
        napraviTabelu.executeUpdate("alter table grad add drzava integer references drzava(id);");
        napraviTabelu.executeUpdate("alter table drzava add glavni_grad integer references grad(id);");

        Grad g = new Grad("London", 8825000, null);
        Drzava d = new Drzava("Velika Britanija", g);
        g.setDrzava(d);
        dodajGrad(g);
        dodajDrzavu(d);


        g.setNaziv("Pariz");
        g.setBrStanovnika(2206488);
        d.setNaziv("Francuska");
        d.setGlavniGrad(g);
        dodajGrad(g);
        dodajDrzavu(d);

        g.setNaziv("Beč");
        g.setBrStanovnika(1899055);
        d.setNaziv("Austrija");
        d.setGlavniGrad(g);
        dodajGrad(g);
        dodajDrzavu(d);

        g.setNaziv("Manchester");
        g.setBrStanovnika(545500);
        dodajGrad(g);

        g.setNaziv("Graz");
        g.setBrStanovnika(280200);
        dodajGrad(g);

        Statement smt = con.createStatement();
        ResultSet r = smt.executeQuery("select * from grad");
        while (r.next()){
            System.out.println(r.getString(2));
        }

    }

    public static GeografijaDAO getInstance() throws SQLException {
        //if(instance == null) initialize();
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
        ResultSet postoji = statement.executeQuery("SELECT id FROM grad WHERE naziv = '" + grad.getNaziv() + "'");
        if(postoji.next()) return;

        PreparedStatement upit = con.prepareStatement("INSERT INTO grad (naziv, broj_stanovnika, drzava) VALUES (?, ?, null)");
        upit.setString(1,grad.getNaziv());
        upit.setInt(2,grad.getBrStanovnika());

        //statement = con.createStatement();
        //ovim upitom dobijem id drzave u kojoj se grad iz parametra metode nalazi
        ResultSet drzavaId = statement.executeQuery("SELECT id FROM drzava WHERE naziv = '" + grad.getDrzava().getNaziv() + "'");

        upit.execute();

        upit = con.prepareStatement("update grad set drzava = ? where naziv = ?;");


        //ako nema te drzave
        //treba dodati nju i njen glavni grad
        //nadajmo se da to radi ova metoda
        if(!drzavaId.next()) {
            dodajDrzavu(grad.getDrzava());
            //uzmi taj dodani id

        }else{
            upit.setInt(1, drzavaId.getInt(1));
            upit.setString(2, grad.getNaziv());

            upit.executeUpdate();
        }

    }

    private void dodajDrzavu(Drzava drzava) throws SQLException {
        //provjeriti da li vec postoji
        statement = con.createStatement();
        ResultSet postoji = statement.executeQuery("SELECT id FROM drzava WHERE naziv = '" + drzava.getNaziv() + "'");
        if (postoji.next()) return;

        PreparedStatement upit = con.prepareStatement("INSERT INTO drzava (naziv, glavni_grad) VALUES (?, ?)");
        upit.setString(1, drzava.getNaziv());

        //dobijem id glavnog grada drzava iz parametra metode
        ResultSet gradId = statement.executeQuery("SELECT id FROM grad WHERE naziv = '" + drzava.getGlavniGrad().getNaziv() + "'");

        //ako taj gl grad ne postoji
        boolean azuriraj = false;
        if(!gradId.next()) {
            azuriraj = true;
            upit.setNull(2, Types.INTEGER);
            //ovime se izbjegne da ne dodje do beskonacnog pozivanja dodaj grad dodaj drzavu
        }
        else
            upit.setInt(2, gradId.getInt(1));

        upit.executeUpdate();

        if(azuriraj) {
            //sad je drzava dodana pa mozemo dodati grad
            dodajGrad(drzava.getGlavniGrad());
            //uzmi id dodanog grada
            ResultSet grad = statement.executeQuery("SELECT id FROM grad WHERE naziv = '" + drzava.getGlavniGrad().getNaziv() + "'");

            //postavi gl grad na dodani grad (sto je bilo null)
            statement.executeUpdate("UPDATE drzava SET glavni_grad = " + grad.getInt(1) + " WHERE naziv = '" + drzava.getNaziv() + "'");
        }
    }

    public Drzava nadjiDrzavu(String drzava) throws SQLException {
        procitajDrzavu.setString(1,drzava);
        ResultSet drzavaId = procitajDrzavu.executeQuery();

        if(!drzavaId.next()) return null;

        Drzava d = new Drzava(drzava, glavniGrad(drzava));

        return d;
    }

    public void izmijeniGrad(Grad grad) throws SQLException {
        ResultSet g = statement.executeQuery("SELECT id FROM grad WHERE naziv = '" + grad.getNaziv() + "'");
        if(!g.next()) return; //grad ne postoji nema se sta izmijeniti

        statement = con.createStatement();
        statement.executeUpdate("UPDATE grad SET broj_stanovnika = " + grad.getBrStanovnika() +
                "WHERE naziv = '" + grad.getNaziv() + "'");

        procitajDrzavu.setString(1,grad.getDrzava().getNaziv());
        ResultSet d = procitajDrzavu.executeQuery();
        if(!d.next()) {
            dodajDrzavu(grad.getDrzava()); //drzava ne postoji pa je treba dodati
            d = procitajDrzavu.executeQuery(); //sad uzmi id
        }

        statement.executeUpdate("UPDATE grad SET drzava = " + d.getInt(1) +
                "WHERE naziv = '" + grad.getDrzava().getNaziv() + "'"); //azuriraj drzavz
    }
}
