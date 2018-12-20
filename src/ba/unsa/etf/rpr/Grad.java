package ba.unsa.etf.rpr;

public class Grad {
    private String naziv;
    private int brStanovnika;
    private Drzava drzava;

    public Grad(){
        setNaziv(null);
        setBrStanovnika(0);
        setDrzava(null);
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public int getBrStanovnika() {
        return brStanovnika;
    }

    public void setBrStanovnika(int brStanovnika) {
        this.brStanovnika = brStanovnika;
    }

    public Drzava getDrzava() {
        return drzava;
    }

    public void setDrzava(Drzava drzava) {
        this.drzava = drzava;
    }
}
