import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.brunocvcunha.opennode.api.OpenNodeService;
import org.brunocvcunha.opennode.api.OpenNodeServiceFactory;
import org.brunocvcunha.opennode.api.model.OpenNodeCharge;
import org.brunocvcunha.opennode.api.model.OpenNodeCreateCharge;
import org.brunocvcunha.opennode.api.model.OpenNodeCurrency;

public class BitcoinFoodBillFrameReceipt extends JFrame {
	private static final int FRAME_WIDTH = 900;
	private static final int FRAME_HEIGHT = 800;

	// FILE OUTPUT STREAM
	String fileName;
	FileWriter writer;

	// MENU BAR
	private JMenuBar menuBar;
	private JMenuItem selected = createServer("Cody");

	// PANELS
	private JPanel northPanel;
	private JPanel taxPanel;
	private JPanel tipPanel;
	private JPanel centerPanel;
	private JPanel menuPanel;
	private JPanel billPanel;
	private JPanel southPanel;
	private JPanel totalsPanel;
	private JPanel totalButtonPanel;

	// TAX
	private ArrayList<JRadioButton> radioButtons;
	private String[] states = { "AZ", "CA", "CO", "NM", "NV" };
	private double[] taxRates = { 5.6, 7.25, 2.9, 5.125, 6.85 };
	private static JTextField taxField;

	// SLIDER
	private JSlider slider;
	private String sliderValue;
	private JTextField sliderValueLabel;

	// MENU
	private ArrayList<JButton> menuButtons;
	private ArrayList<Double> selectedItems;
	private String[] menuItems = { "Shrimp", "Filet", "Butter Cake", "Calamari", "Sea Bass", "Choc. Gelato",
			"Chopped Salad", "Lobster Tails (2)", "Bread Pudding", "Crab Cakes", "NY Strip", "Choc. Lava Cake",
			"BLT Wedge", "Ahi Fillet", "Strawb. Sorbet" };
	private double[] prices = { 27.00, 68.00, 19.00, 21.00, 54.00, 11.00, 14, 90.00, 15.00, 49.00, 53.00, 14.00, 15.00,
			53.00, 11.00 };

	// BILL
	private JTextArea billArea;
	private int clickedItemIndex;

	// TOTALS
	private JLabel total;
	private JLabel totalWithTax;
	private JLabel totalWithTip;
	double totalDouble;
	double baseTotal;
	double taxTotal;
	double tipTotal;
	double TOTAL;

	// TOTAL BUTTON
	private JButton totalButton;

	class FileMenuListener implements ActionListener {
		JMenuItem fileMenuButton;

		public FileMenuListener(JMenuItem j) {
			fileMenuButton = j;
		}

		public void actionPerformed(ActionEvent e) {
			if (fileMenuButton.getText().equalsIgnoreCase("Clear")) {
				resetVariables();
				enableJcomponents();
			}

			if (fileMenuButton.getText().equalsIgnoreCase("Exit")) {
				System.exit(0);
			}
		}
	}

	class ServerListener implements ActionListener {
		JMenuItem clicked;

		public ServerListener(JMenuItem clicked) {
			this.clicked = clicked;
		}

		public void actionPerformed(ActionEvent e) {
			selected = clicked;
		}
	}

	class RadioButtonListener implements ActionListener {
		private int stateIndex;

		public RadioButtonListener(int stateIndex) {
			this.stateIndex = stateIndex;
		}

		public void actionPerformed(ActionEvent e) {
			taxField.setText(String.valueOf(taxRates[stateIndex]));

			baseTotal = 0;
			taxTotal = 0;
			tipTotal = 0;
			getTotals();
		}

	}

	class SliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			sliderValueLabel.setText(String.valueOf(slider.getValue()));

			baseTotal = 0;
			taxTotal = 0;
			tipTotal = 0;
			getTotals();
		}
	}

	class MenuItemListener implements ActionListener {
		private int itemIndex;

		public MenuItemListener(int itemIndex) {
			this.itemIndex = itemIndex;

		}

		public void actionPerformed(ActionEvent e) {
			clickedItemIndex = itemIndex;

			selectedItems.add(prices[clickedItemIndex]);

			String s = String.format("%-34s %-2s %5.2f", menuItems[clickedItemIndex], "$", prices[clickedItemIndex]);
			billArea.append(s + "\n");

			getTotals();
		}
	}

	class TotalListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			disableJcomponents();
			completeReceipt();
			getTime();
			writeToFile();
		}
	}

	public BitcoinFoodBillFrameReceipt() {
		// NORTH PANEL
		northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		add(northPanel, BorderLayout.NORTH);

		// TAX
		taxPanel = new JPanel();
		taxPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
		northPanel.add(taxPanel, BorderLayout.NORTH);

		// TIP
		tipPanel = new JPanel();
		tipPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
		northPanel.add(tipPanel, BorderLayout.SOUTH);

		// CENTER PANEL
		centerPanel = new JPanel();
		centerPanel.setLayout(new GridLayout(0, 2));
		add(centerPanel, BorderLayout.CENTER);

		// MENU
		menuPanel = new JPanel();
		GridLayout menuLayout = new GridLayout(6, 3);
		menuLayout.setVgap(10);
		menuLayout.setHgap(10);
		menuPanel.setLayout(menuLayout);
		centerPanel.add(menuPanel);

		// BILL
		billPanel = new JPanel();
		billPanel.setLayout(new GridLayout(0, 1));
		centerPanel.add(billPanel);

		// SOUTH PANEL
		southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		add(southPanel, BorderLayout.SOUTH);

		// TOTALS
		totalsPanel = new JPanel();
		totalsPanel.setLayout(new GridLayout(3, 2));
		southPanel.add(totalsPanel, BorderLayout.WEST);

		// TOTAL BUTTON
		totalButtonPanel = new JPanel();
		totalButtonPanel.setLayout(new GridLayout(0, 1));
		southPanel.add(totalButtonPanel, BorderLayout.SOUTH);

		createMenuBar();
		createTaxPanel();
		createTipPanel();
		createMenuPanel();
		createBillPanel();
		createTotalLabels();
		createTotalButton();
		setSettings();
	}

	public void createMenuBar() {
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		menuBar.add(createFileMenu());
		menuBar.add(createServerMenu());
	}

	public JMenu createFileMenu() {
		JMenu fileMenu = new JMenu("File");

		JMenuItem clear = new JMenuItem("Clear");
		clear.addActionListener(new FileMenuListener(clear));
		fileMenu.add(clear);

		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new FileMenuListener(exit));
		fileMenu.add(exit);

		return fileMenu;
	}

	public JMenu createServerMenu() {
		JMenu serverMenu = new JMenu("Servers");

		serverMenu.add(createServer("Cody W."));
		serverMenu.add(createServer("Alyssa W."));
		serverMenu.add(createServer("Bella F."));
		serverMenu.add(createServer("Chloe J."));
		serverMenu.add(createServer("Satoshi Nakamoto"));

		return serverMenu;
	}

	public JMenuItem createServer(String name) {
		JMenuItem server = new JMenuItem();

		server.setText(name);
		server.addActionListener(new ServerListener(server));

		return server;
	}

	public void createTaxPanel() {
		radioButtons = new ArrayList<JRadioButton>();

		JLabel taxLabel = new JLabel("Tax Rate:");
		taxPanel.add(taxLabel);

		ButtonGroup group = new ButtonGroup();

		for (int i = 0; i < states.length; i++) {
			JRadioButton button = new JRadioButton(states[i]);
			button.addActionListener(new RadioButtonListener(i));

			radioButtons.add(button);
			group.add(button);

			if (i == 0)
				button.setSelected(true);
		}

		for (int i = 0; i < states.length; i++) {
			taxPanel.add(radioButtons.get(i));
		}

		taxField = new JTextField(4);
		taxField.setEnabled(false);
		taxField.setText("" + taxRates[0]);
		taxPanel.add(taxField);

		JLabel percentLabel = new JLabel("%");
		taxPanel.add(percentLabel);
	}

	public void createTipPanel() {
		JLabel tipLabel = new JLabel("Tip");
		tipPanel.add(tipLabel);

		slider = new JSlider(0, 35);
		slider.setValue(15);
		tipPanel.add(slider);
		slider.addChangeListener(new SliderListener());

		sliderValueLabel = new JTextField(2);
		sliderValueLabel.setEnabled(false);
		sliderValueLabel.setText("15");
		tipPanel.add(sliderValueLabel);

		JLabel percentLabel2 = new JLabel("%");
		tipPanel.add(percentLabel2);
	}

	public void createMenuPanel() {
		menuButtons = new ArrayList<JButton>();
		selectedItems = new ArrayList<Double>();

		menuPanel.add(new JLabel("              App."));
		menuPanel.add(new JLabel("              Main"));
		menuPanel.add(new JLabel("          Dessert"));

		for (int i = 0; i < menuItems.length; i++) {
			JButton button = new JButton(menuItems[i]);
			button.addActionListener(new MenuItemListener(i));
			menuButtons.add(button);
		}

		for (int i = 0; i < menuButtons.size(); i++) {
			menuPanel.add(menuButtons.get(i));
		}
	}

	public void createBillPanel() {
		billArea = new JTextArea();

		Font font = new Font("Monospaced", Font.BOLD, 12);
		billArea.setFont(font);
		billArea.setEditable(false);
		billArea.setLineWrap(true);
		billArea.setMargin(new Insets(10, 10, 10, 10));

		JScrollPane scrollPane = new JScrollPane(billArea);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		billPanel.add(scrollPane);
	}

	public void createTotalLabels() {
		JLabel totalLabel = new JLabel("    Current Total:   ");
		totalsPanel.add(totalLabel);

		total = new JLabel();
		totalsPanel.add(total);

		JLabel withTaxLabel = new JLabel("    Current Total w/Tax:   ");
		totalsPanel.add(withTaxLabel);

		totalWithTax = new JLabel();
		totalsPanel.add(totalWithTax);

		JLabel withTipLabel = new JLabel("    Current Total w/Tip:   ");
		totalsPanel.add(withTipLabel);

		totalWithTip = new JLabel();
		totalsPanel.add(totalWithTip);
	}

	public void createTotalButton() {
		totalButton = new JButton("Total");
		totalButton.addActionListener(new TotalListener());
		totalButtonPanel.add(totalButton);
	}

	public void setSettings() {
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setTitle("Enhanced Restaurant Bill – List Receipt");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	// HELPER METHODS

	public void getTotals() {
		baseTotal = 0;
		for (int i = 0; i < selectedItems.size(); i++) {
			baseTotal += selectedItems.get(i);
		}

		// HANDLE BASE TOTAL
		baseTotal = Math.round(baseTotal * 100.00) / 100.00;
		String formattedBaseTotal = String.format("%s %.2f", "$", baseTotal);
		total.setText(formattedBaseTotal);

		// HANDLE TOTAL W/TAX
		taxTotal = baseTotal + (baseTotal * Double.parseDouble(taxField.getText()) / 100.00);
		String formattedTaxTotal = String.format("%s %.2f", "$", taxTotal);
		totalWithTax.setText(formattedTaxTotal);

		// HANDLE TOTAL W/TIP
		tipTotal = taxTotal;
		tipTotal += (baseTotal * (Double.parseDouble(sliderValueLabel.getText()) / 100.00));
		String formattedTipTotal = String.format("%s %.2f", "$", tipTotal);
		totalWithTip.setText(formattedTipTotal);

	}

	public void resetVariables() {
		baseTotal = 0;
		total.setText(null);

		taxTotal = 0;
		totalWithTax.setText(null);

		tipTotal = 0;
		totalWithTip.setText(null);

		slider.setValue(15);
		sliderValueLabel.setText("15");

		selectedItems.clear();

		billArea.setText(null);
	}

	public void enableJcomponents() {
		for (JRadioButton button : radioButtons) {
			button.setEnabled(true);
		}

		slider.setEnabled(true);

		for (JButton button : menuButtons) {
			button.setEnabled(true);
		}

		totalButton.setEnabled(true);
	}

	public void disableJcomponents() {
		for (JRadioButton button : radioButtons) {
			button.setEnabled(false);
		}

		slider.setEnabled(false);

		for (JButton button : menuButtons) {
			button.setEnabled(false);
		}

		totalButton.setEnabled(false);
	}

	public void completeReceipt() {
		String separator = "------------------------------------ ---------";
		billArea.append(separator + "\n");

		String preTaxTotal = String.format("%-34s %-2s %5.2f", "Pretax Total", "$", baseTotal);
		billArea.append(preTaxTotal + "\n");
		String tax = String.format("%-34s %-2s %5.2f", "Tax", "$",
				baseTotal * Double.parseDouble(taxField.getText()) / 100.00);
		billArea.append(tax + "\n");

		billArea.append(separator + "\n");

		String postTaxTotal = String.format("%-34s %-2s %5.2f", "Total w/Tax", "$",
				baseTotal + (baseTotal * Double.parseDouble(taxField.getText()) / 100.00));
		billArea.append(postTaxTotal + "\n");

		String tipOnly = String.format("%-34s %-2s %5.2f", "Tip", "$",
				(baseTotal * Double.parseDouble(sliderValueLabel.getText()) / 100.00));
		billArea.append(tipOnly + "\n");

		billArea.append(separator + "\n");

		String finalTotal = String.format("%-34s %-2s %5.2f", "Total", "$",
				(baseTotal + (baseTotal * Double.parseDouble(taxField.getText()) / 100.00))
						+ (baseTotal * Double.parseDouble(sliderValueLabel.getText()) / 100.00));

		TOTAL = (baseTotal + (baseTotal * Double.parseDouble(taxField.getText()) / 100.00))
				+ (baseTotal * Double.parseDouble(sliderValueLabel.getText()) / 100.00);
		System.out.println(TOTAL);

		billArea.append(finalTotal + "\n");

		OpenNodeService service = OpenNodeServiceFactory.buildClient("e6c98559-e1a8-473a-abcf-0dfb73211df1");

		OpenNodeCreateCharge createCharge = OpenNodeCreateCharge.builder().orderId("ABC-123")
				.description("OpenNode Java Client").amount(TOTAL).currency(OpenNodeCurrency.USD).build();

		try {
			OpenNodeCharge createdCharge = service.createCharge(createCharge).execute().body().getData();

			System.out.println("Created charge with ID: " + createdCharge.getId());
			System.out.println(createdCharge.getLightningInvoice().getPayreq());
			billArea.append("\n\nLightning Invoice:\n");
			billArea.append("\n" + createdCharge.getLightningInvoice().getPayreq() + "\n");

			System.out.println(createdCharge.getChainInvoice().getAddress());
			billArea.append("\nOnChain Address:\n");
			billArea.append("\n" + createdCharge.getChainInvoice().getAddress());

		} catch (IOException e) {
			e.printStackTrace();
		}

		billArea.append("\n\n\n");

		String signature = String.format("It was a pleasure serving you!\n");
		billArea.append(signature + "    -- " + selected.getText() + "\n");
	}

	public void getTime() {
		LocalTime time = LocalTime.now();
		LocalDate date = LocalDate.now();

		fileName = "Receipt " + date + "-" + time;
		fileName = fileName.replace(':', '-');
		fileName = fileName.substring(0, 27);
		fileName = fileName + ".txt";
	}

	public void writeToFile() {
		try {
			writer = new FileWriter(fileName);

			Scanner bill = new Scanner(billArea.getText());

			while (bill.hasNextLine()) {
				String line = bill.nextLine();
				writer.write(line + "\n");
			}

			bill.close();
			writer.close();
		} catch (IOException exception) {
			System.out.println(exception);
		}
	}

	public static void main(String[] args) {
		JFrame frame = new BitcoinFoodBillFrameReceipt();
	}
}