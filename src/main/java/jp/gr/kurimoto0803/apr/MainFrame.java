
package jp.gr.kurimoto0803.apr;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * http://webservices.amazon.co.jp/scratchpad/index.html#
 * 
 * ここ見たら開発楽
 * 
 * @author kuri
 *
 */
public class MainFrame extends JFrame {

	private static final long serialVersionUID = -5750180976305794647L;

	private JTextPane sellerNumTextPane;
	private JTextPane keywordsTextPane;
	private JTextPane rankingFromTextPane;
	private JTextPane rankingToTextPane;
	private JComboBox<String> categoryComboBox;
	private JButton searchButton;
	private DefaultTableModel searchResult;
	private JLabel fromPageLabel;
	private JLabel toPageLabel;

	private static enum COL {
		NAME(0), CATEGORY(1), BRAND(2), NEW(3), USED(4), VARIATION(5), RANKING(6), LOWEST_NEW_PRICE(
				7), LOWEST_VARIATION_PRICE(8), URL(9), ASIN(10);

		private int col;

		private COL(int col) {
			this.col = col;
		}

		private int val() {
			return this.col;
		}
	};

	public static void main(String[] args) throws Exception {
		new MainFrame().setVisible(true);
	}

	public MainFrame() throws Exception {
		super("AmazonProductResearcher v1.4");
		setSize(800, 600);
		// setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
		setLocationRelativeTo(null);
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setContentPane(getMainPanel());
	}

	private JPanel getMainPanel() throws Exception {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(getConditionPanel(), BorderLayout.NORTH);
		panel.add(getSearchPanel(), BorderLayout.CENTER);
		return panel;
	}

	public JLabel getFromPageLabel() {
		if (fromPageLabel == null) {
			fromPageLabel = new JLabel();
		}
		return fromPageLabel;
	}

	public JLabel getToPageLabel() {
		if (toPageLabel == null) {
			toPageLabel = new JLabel();
		}
		return toPageLabel;
	}

	private JPanel getSearchPanel() throws Exception {
		JPanel panel = new JPanel(new BorderLayout());

		JPanel pagingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		pagingPanel.add(getSearchButton());
		pagingPanel.add(getFromPageLabel());
		pagingPanel.add(new JLabel(" / "));
		pagingPanel.add(getToPageLabel());
		pagingPanel.add(getNextButton());
		panel.add(pagingPanel, BorderLayout.NORTH);
		panel.add(getTablePanel(), BorderLayout.CENTER);
		return panel;
	}

	private JScrollPane getTablePanel() {
		JTable table = new JTable();
		table.setModel(getSearchResult());
		table.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = table.convertRowIndexToModel(table.getSelectedRow());
				int col = table.convertColumnIndexToModel(table.getSelectedColumn());
				if (col == COL.URL.val()) {
					String url = (String) table.getModel().getValueAt(row, col);
					Desktop desktop = Desktop.getDesktop();
					try {
						URI uri = new URI(url);
						desktop.browse(uri);
					} catch (URISyntaxException | IOException e2) {
						e2.printStackTrace();
					}
				}
			}
		});
		setTableSorter(table);
		setColumnSize(table);
		JScrollPane panel = new JScrollPane();
		panel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		panel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		panel.setViewportView(table);
		return panel;
	}

	private void setColumnSize(JTable table) {
		DefaultTableColumnModel columnModel = (DefaultTableColumnModel) table.getColumnModel();
		columnModel.getColumn(COL.NAME.val()).setPreferredWidth(200);
		columnModel.getColumn(COL.CATEGORY.val()).setPreferredWidth(100);
		columnModel.getColumn(COL.BRAND.val()).setPreferredWidth(100);
		columnModel.getColumn(COL.NEW.val()).setPreferredWidth(50);
		columnModel.getColumn(COL.USED.val()).setPreferredWidth(50);
		columnModel.getColumn(COL.VARIATION.val()).setPreferredWidth(50);
		columnModel.getColumn(COL.RANKING.val()).setPreferredWidth(50);
		columnModel.getColumn(COL.LOWEST_NEW_PRICE.val()).setPreferredWidth(50);
		columnModel.getColumn(COL.LOWEST_VARIATION_PRICE.val()).setPreferredWidth(50);
		columnModel.getColumn(COL.URL.val()).setPreferredWidth(100);
		columnModel.getColumn(COL.ASIN.val()).setPreferredWidth(100);
	}

	private static class IntComparator implements Comparator<String> {
		public int compare(String a, String b) {
			return Integer.parseInt(a) - Integer.parseInt(b);
		}
	}

	private void setTableSorter(JTable table) {
		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(getSearchResult());
		sorter.setComparator(COL.NEW.val(), new IntComparator());
		sorter.setComparator(COL.USED.val(), new IntComparator());
		sorter.setComparator(COL.VARIATION.val(), new IntComparator());
		sorter.setComparator(COL.RANKING.val(), new IntComparator());
		sorter.setComparator(COL.LOWEST_NEW_PRICE.val(), new IntComparator());
		sorter.setComparator(COL.LOWEST_VARIATION_PRICE.val(), new IntComparator());
		table.setRowSorter(sorter);
	}

	private DefaultTableModel getSearchResult() {
		if (searchResult == null) {
			searchResult = new DefaultTableModel();
			Vector<String> header = new Vector<>();
			header.add("Name");
			header.add("Category");
			header.add("Brand");
			header.add("New");
			header.add("Used");
			header.add("Variation");
			header.add("Ranking");
			header.add("LowestNewPrice");
			header.add("LowestVariationPrice");
			header.add("URL");
			header.add("ASIN");
			searchResult.setColumnIdentifiers(header);
		}
		return searchResult;
	}

	private JPanel getConditionPanel() throws Exception {
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		searchPanel.add(getTitlePanel("キーワード", getKeywordsTextPane()));
		searchPanel.add(getTitlePanel("カテゴリー", getCategoryComboBox()));
		searchPanel.add(getTitlePanel("Rank", getRankingToTextPane()));
		searchPanel.add(getTitlePanel("～", getRankingFromTextPane()));
		searchPanel.add(getTitlePanel("New+Variation <=", getSellerNumTextPane()));
		return searchPanel;
	}

	private JButton getSearchButton() throws Exception {
		if (searchButton == null) {
			searchButton = new JButton("Search");
			searchButton.addActionListener(new SearchActionListener(this, true) {

				@Override
				public void callback(List<Item> items, int presentPage, int totalPages) {
					getFromPageLabel().setText(String.valueOf(presentPage));
					getToPageLabel().setText(String.valueOf(totalPages));
					getSearchResult().setRowCount(0);
					addItems(items);
				}

			});
		}
		return searchButton;
	}

	private JButton getNextButton() throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException {
		JButton nextButton = new JButton("Next");
		nextButton.addActionListener(new SearchActionListener(this, false) {
			@Override
			public void callback(List<Item> items, int presentPage, int totalPages) {
				getFromPageLabel().setText(String.valueOf(presentPage));
				addItems(items);
			}

		});
		return nextButton;
	}

	private void addItems(List<Item> items) {
		if (items == null) {
			return;
		}
		for (Item item : items) {
			getSearchResult().addRow(new String[] { item.getName(), item.getCategory(), item.getBrand(),
					String.valueOf(item.getOfferNew()), String.valueOf(item.getOfferUsed()),
					String.valueOf(item.getOfferVariation()), String.valueOf(item.getRanking()),
					String.valueOf(item.getLowestNewPrice()), String.valueOf(item.getLowestVariationPrice()),
					item.getUrl(), item.getASIN() });
		}
	}

	public JTextPane getSellerNumTextPane() {
		if (sellerNumTextPane == null) {
			sellerNumTextPane = new JTextPane();
			sellerNumTextPane.setPreferredSize(new Dimension(40, 22));
			sellerNumTextPane.setText("5");
		}
		return sellerNumTextPane;
	}

	public JTextPane getRankingFromTextPane() {
		if (rankingFromTextPane == null) {
			rankingFromTextPane = new JTextPane();
			rankingFromTextPane.setPreferredSize(new Dimension(40, 22));
			rankingFromTextPane.setText("20000");
		}
		return rankingFromTextPane;
	}

	public JTextPane getRankingToTextPane() {
		if (rankingToTextPane == null) {
			rankingToTextPane = new JTextPane();
			rankingToTextPane.setPreferredSize(new Dimension(40, 22));
			rankingToTextPane.setText("1");
		}
		return rankingToTextPane;
	}

	public JComboBox<String> getCategoryComboBox() {
		if (categoryComboBox == null) {
			categoryComboBox = new JComboBox<String>();
			categoryComboBox.addItem("All");
			categoryComboBox.addItem("Apparel");
			categoryComboBox.addItem("Automotive");
			categoryComboBox.addItem("Baby");
			categoryComboBox.addItem("Beauty");
			categoryComboBox.addItem("Blended");
			categoryComboBox.addItem("Books");
			categoryComboBox.addItem("Classical");
			categoryComboBox.addItem("DVD");
			categoryComboBox.addItem("Electronics");
			categoryComboBox.addItem("ForeignBooks");
			categoryComboBox.addItem("Grocery");
			categoryComboBox.addItem("HealthPersonalCare");
			categoryComboBox.addItem("Hobbies");
			categoryComboBox.addItem("HomeImprovement");
			categoryComboBox.addItem("Jewelry");
			categoryComboBox.addItem("Kitchen");
			categoryComboBox.addItem("Music");
			categoryComboBox.addItem("MusicTracks");
			categoryComboBox.addItem("OfficeProducts");
			categoryComboBox.addItem("Shoes");
			categoryComboBox.addItem("Software");
			categoryComboBox.addItem("SportingGoods");
			categoryComboBox.addItem("Toys");
			categoryComboBox.addItem("VHS");
			categoryComboBox.addItem("Video");
			categoryComboBox.addItem("VideoGames");
			categoryComboBox.addItem("Watches");
			categoryComboBox.setSelectedIndex(0);
		}
		return categoryComboBox;
	}

	public JTextPane getKeywordsTextPane() {
		if (keywordsTextPane == null) {
			keywordsTextPane = new JTextPane();
			keywordsTextPane.setPreferredSize(new Dimension(150, 22));
		}
		return keywordsTextPane;
	}

	private JPanel getTitlePanel(String title, JComponent comp) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		panel.add(new JLabel(title));
		panel.add(comp);
		return panel;
	}

}
