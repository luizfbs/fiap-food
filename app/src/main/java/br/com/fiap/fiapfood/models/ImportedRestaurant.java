package br.com.fiap.fiapfood.models;

public class ImportedRestaurant {

    public String NOMERESTAURANTE;
    public String TELEFONE;
    public String TIPO;
    public Double CustoMedio;
    public String OBSERVACAO;
    public String LOCALIZACAO;

    public String getName(){
        return NOMERESTAURANTE;
    }

    public String getPhone(){
        return TELEFONE;
    }

    public double getLatitude(){
        return Double.parseDouble(LOCALIZACAO.split(",")[0]);
    }

    public double getLongitude(){
        return Double.parseDouble(LOCALIZACAO.split(",")[1]);
    }

    public String getNotes() {
        return OBSERVACAO;
    }

    public String getType() {
        return TIPO;
    }

    public double getCost() {
        return CustoMedio;
    }
}
