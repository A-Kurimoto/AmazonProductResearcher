package jp.gr.kurimoto0803.apr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class SearchActionListener implements ActionListener {
	private static final String AWS_ACCESS_KEY_ID = "xxx";
	private static final String AWS_SECRET_KEY = "xxx";
	private static final String ASSOCIATE_TAG = "xxx";
	private static final String ENDPOINT = "webservices.amazon.co.jp";
	private SignedRequestsHelper helper;
	private MainFrame mainFrame;
	private boolean isNewSearch;

	public SearchActionListener(MainFrame mainFrame, boolean isNewSearch) throws InvalidKeyException,
			IllegalArgumentException, UnsupportedEncodingException, NoSuchAlgorithmException {
		this.mainFrame = mainFrame;
		this.isNewSearch = isNewSearch;
		this.helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
	}

	public abstract void callback(List<Item> items, int presentPage, int totalPages);

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			int presentPage = 0;
			if (isNewSearch) {
				presentPage = 1;
			} else {
				String from = mainFrame.getFromPageLabel().getText();
				if (from.equals("")) {
					return;
				}
				if (Integer.parseInt(from) <= Integer.parseInt(mainFrame.getToPageLabel().getText())) {
					presentPage = Integer.parseInt(from) + 1;
				} else {
					return;
				}
			}
			String keywords = mainFrame.getKeywordsTextPane().getText();
			if (keywords.equals("")) {
				JOptionPane.showMessageDialog(null, "キーワードを入力してください。");
				return;
			}
			String requestUrl = getItemSearchUrl(keywords, (String) mainFrame.getCategoryComboBox().getSelectedItem(),
					presentPage);
			System.out.println("Signed URL: \"" + requestUrl + "\"");

			Document itemSearchDoc = getDocument(requestUrl);

			System.out.println("Successed");
			NodeList list = itemSearchDoc.getElementsByTagName("TotalPages");
			if (list.getLength() == 0) {
				callback(null, presentPage, Integer.parseInt(mainFrame.getToPageLabel().getText()));
				return;
			}
			setSearchResult(presentPage, itemSearchDoc, list);
		} catch (ParserConfigurationException | SAXException | IOException e1) {
			e1.printStackTrace();
		}
	}

	private void setSearchResult(int presentPage, Document itemSearchDoc, NodeList list) {
		int totalPages = Integer.parseInt(list.item(0).getTextContent());
		NodeList itemNodeList = itemSearchDoc.getElementsByTagName("Item");
		List<Item> result = new ArrayList<>();
		for (int i = 0; i < itemNodeList.getLength(); i++) {
			Item itemObj = new Item();
			setItem(itemObj, itemNodeList.item(i));
			setMatchingResult(result, itemObj);
		}
		callback(result, presentPage, totalPages);
	}

	private void setMatchingResult(List<Item> result, Item itemObj) {
		if (itemObj.getRanking() != 0
				&& itemObj.getRanking() <= Integer.parseInt(mainFrame.getRankingFromTextPane().getText())
				&& itemObj.getRanking() >= Integer.parseInt(mainFrame.getRankingToTextPane().getText())) {
			if ((itemObj.getOfferNew() + itemObj.getOfferVariation()) <= Integer
					.parseInt(mainFrame.getSellerNumTextPane().getText())) {
				result.add(itemObj);
			}
		}
	}

	private void setItem(Item itemObj, Node item) {
		NodeList childs = item.getChildNodes();
		for (int j = 0; j < childs.getLength(); j++) {
			Node childNode = childs.item(j);
			if (childNode.getNodeName().equals("ASIN")) {
				itemObj.setASIN(childNode.getTextContent());
			} else if (childNode.getNodeName().equals("DetailPageURL")) {
				itemObj.setUrl(childNode.getTextContent());
			} else if (childNode.getNodeName().equals("SalesRank")) {
				itemObj.setRanking(Integer.parseInt(childNode.getTextContent()));
			} else if (childNode.getNodeName().equals("ItemAttributes")) {
				setItemAttributes(itemObj, childNode);
			} else if (childNode.getNodeName().equals("OfferSummary")) {
				setOfferSummary(itemObj, childNode);
			} else if (childNode.getNodeName().equals("Variations")) {
				setVariations(itemObj, childNode);
			} else if (childNode.getNodeName().equals("VariationSummary")) {
				setVariationSummary(itemObj, childNode);
			}
		}
	}

	private void setVariationSummary(Item itemObj, Node childNode) {
		NodeList summaries = childNode.getChildNodes();
		for (int k = 0; k < summaries.getLength(); k++) {
			Node summaryNode = summaries.item(k);
			if (summaryNode.getNodeName().equals("LowestPrice")) {
				NodeList prices = summaryNode.getChildNodes();
				for (int l = 0; l < prices.getLength(); l++) {
					Node priceNode = prices.item(l);
					if (priceNode.getNodeName().equals("Amount")) {
						itemObj.setLowestVariationPrice(Integer.parseInt(priceNode.getTextContent()));
						break;
					}
				}
			}
		}
	}

	private void setVariations(Item itemObj, Node childNode) {
		NodeList variations = childNode.getChildNodes();
		for (int k = 0; k < variations.getLength(); k++) {
			Node variationNode = variations.item(k);
			if (variationNode.getNodeName().equals("TotalVariations")) {
				itemObj.setOfferVariation(Integer.parseInt(variationNode.getTextContent()));
				break;
			}

		}
	}

	private void setItemAttributes(Item itemObj, Node childNode) {
		NodeList attrs = childNode.getChildNodes();
		for (int k = 0; k < attrs.getLength(); k++) {
			Node attrNode = attrs.item(k);
			if (attrNode.getNodeName().equals("Title")) {
				itemObj.setName(attrNode.getTextContent());
			} else if (attrNode.getNodeName().equals("Binding")) {
				itemObj.setCategory(attrNode.getTextContent());
			} else if (attrNode.getNodeName().equals("Brand")) {
				itemObj.setBrand(attrNode.getTextContent());
			}
		}
	}

	private void setOfferSummary(Item itemObj, Node childNode) {
		NodeList summary = childNode.getChildNodes();
		for (int k = 0; k < summary.getLength(); k++) {
			Node offerNode = summary.item(k);
			if (offerNode.getNodeName().equals("TotalNew")) {
				itemObj.setOfferNew(Integer.parseInt(offerNode.getTextContent()));
			} else if (offerNode.getNodeName().equals("TotalUsed")) {
				itemObj.setOfferUsed(Integer.parseInt(offerNode.getTextContent()));
			} else if (offerNode.getNodeName().equals("LowestNewPrice")) {
				NodeList prices = offerNode.getChildNodes();
				for (int l = 0; l < prices.getLength(); l++) {
					Node priceNode = prices.item(l);
					if (priceNode.getNodeName().equals("Amount")) {
						itemObj.setLowestNewPrice(Integer.parseInt(priceNode.getTextContent()));
						break;
					}
				}

			}
		}
	}

	private String getItemSearchUrl(String keywords, String category, int presentPage) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("Service", "AWSECommerceService");
		params.put("AWSAccessKeyId", AWS_ACCESS_KEY_ID);
		params.put("AssociateTag", ASSOCIATE_TAG);
		params.put("Operation", "ItemSearch");
		params.put("ResponseGroup", "ItemAttributes,SalesRank,OfferSummary,VariationOffers");
		params.put("SearchIndex", category);
		if (!category.equals("All") && !category.equals("Beauty") && !category.equals("MusicTracks")) {
			params.put("Sort", "salesrank");
		}
		params.put("Keywords", keywords);
		params.put("ItemPage", String.valueOf(presentPage));
		return helper.sign(params);
	}

	private Document getDocument(String requestUrl) throws ParserConfigurationException, SAXException, IOException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(requestUrl);
		} catch (IOException e) {
			e.printStackTrace();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(requestUrl);
		}
	}
}
