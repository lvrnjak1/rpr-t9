package ba.unsa.etf.rpr;

public class Drzava {
    private String naziv;
    private Grad glavniGrad;

    public Drzava (){
        setNaziv(null);
        setGlavniGrad(null);
    }

    public Drzava(String n, Grad g){
        setNaziv(n);
        setGlavniGrad(g);
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public Grad getGlavniGrad() {
        return glavniGrad;
    }

    public void setGlavniGrad(Grad glavniGrad) {
        this.glavniGrad = glavniGrad;
    }
}
