package jp.gr.kurimoto0803.apr;

public class Item {

	private String name;
	private String category;
	private String brand;
	private String url;
	private String ASIN;
	private int ranking;
	private int offerNew;
	private int offerUsed;
	private int offerVariation;
	private int lowestNewPrice;
	private int lowestVariationPrice;

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public int getLowestNewPrice() {
		return lowestNewPrice;
	}

	public void setLowestNewPrice(int lowestNewPrice) {
		this.lowestNewPrice = lowestNewPrice;
	}

	public int getLowestVariationPrice() {
		return lowestVariationPrice;
	}

	public void setLowestVariationPrice(int lowestVariationPrice) {
		this.lowestVariationPrice = lowestVariationPrice;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getASIN() {
		return ASIN;
	}

	public void setASIN(String ASIN) {
		this.ASIN = ASIN;
	}

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public int getOfferVariation() {
		return offerVariation;
	}

	public void setOfferVariation(int offerVariation) {
		this.offerVariation = offerVariation;
	}

	public int getOfferNew() {
		return offerNew;
	}

	public void setOfferNew(int offerNew) {
		this.offerNew = offerNew;
	}

	public int getOfferUsed() {
		return offerUsed;
	}

	public void setOfferUsed(int offerUsed) {
		this.offerUsed = offerUsed;
	}

}
