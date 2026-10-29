package kivetel;

/** The withdrawal would go over the daily limit. Carries what can still be taken today. */
public class NapiLimitKivetel extends AtmKivetel {

    private final int napiLimit;
    private final int maMegFelveheto;

    public NapiLimitKivetel(int napiLimit, int maMegFelveheto) {
        super("Túllépnéd a napi " + napiLimit + " lejes limitet. Ma még " + maMegFelveheto + " lejt vehetsz fel.");
        this.napiLimit = napiLimit;
        this.maMegFelveheto = maMegFelveheto;
    }

    public int getNapiLimit() {
        return napiLimit;
    }

    public int getMaMegFelveheto() {
        return maMegFelveheto;
    }
}
