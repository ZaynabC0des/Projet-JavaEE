package model;

public enum TuileType {
    VIDE(0),
    VILLE(1),
    FORET(2),
    MONTAGNE(3),
	SOLDAT(4);
	
   private int code;

    TuileType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }


    public static TuileType fromCode(int code) {
        if(code==1) return VILLE;
        else if (code==2) {
            return FORET;
        } else if (code == 3) {
            return MONTAGNE;
        }
        else if (code == 4) {
        	return SOLDAT;
        }

        else{
            return VIDE;
        }
    }
}