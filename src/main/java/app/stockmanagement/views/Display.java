package app.stockmanagement.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicToolBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import app.stockmanagement.controllers.Controller;
import app.stockmanagement.daos.OrderBy;
import app.stockmanagement.exceptions.DateFormatException;
import app.stockmanagement.exceptions.DuplicateValueException;
import app.stockmanagement.exceptions.FormValidationException;
import app.stockmanagement.models.Category;
import app.stockmanagement.models.Goods;
import app.stockmanagement.models.Manufacturer;
import app.stockmanagement.models.Stock;
import app.stockmanagement.utils.StringUtils;
import app.stockmanagement.utils.Utils;

public class Display extends JFrame {

	private static final long serialVersionUID = 1L;

	private static Display display;

	public static Display createDisplay() {
		if (display == null) {
			display = new Display();
		}
		return display;
	}

	public static Display createDisplay(String title, int width, int height) {
		if (display == null) {
			display = new Display(title, width, height);
		}
		return display;
	}

	private String title;
	private int width, height;

	// Top Toolbar
	private TopToolBar tbrTop;
	private JButton btnStock;
	private JButton btnCategory;
	private JButton btnManufacturer;

	// Left Side Panel
	private LeftSidePanel pnlLeft;
	private JCheckBox chkStocks;
	private DefaultTableModel tblStockModel;
	private JTable tblStocks;

	// Right Side Panel
	private RightSidePanel pnlRight;
	private JButton btnAdd, btnEdit, btnRemove, btnSave, btnSkip;
	private JLabel lblSearch, lblOrderBy;
	private JTextField txtSearch;
	private JButton btnSearch, btnExport;
	private JRadioButton rdoManufacturer, rdoCategory;
	private JLabel lblStocks, lblGoodsCode, lblGoodsName, lblCategories, lblManufacturers, lblExpiryDate,
			lblImportedPrice, lblExportedPrice, lblInStock;
	private JComboBox<String> cboStocks, cboCategories, cboManufacturers;
	private JTextField txtGoodsCode, txtGoodsName, txtExpiryDate, txtImportedPrice, txtExportedPrice, txtInStock;
	private DefaultTableModel goodsTableModel;
	private JTable tblGoods;
	private JScrollPane scrllGoodsTable;

	private List<Stock> lstStocks = new ArrayList<>();
	private List<Goods> lstGoods = new ArrayList<>();
	private List<Stock> stocks = new ArrayList<>();
	private List<Category> categories = new ArrayList<>();
	private List<Manufacturer> manufacturers = new ArrayList<>();

	private static final int ADD = 1;
	private static final int EDIT = 2;
	private static final int REMOVE = 3;
	private static final int SAVE = 4;
	private static final int SKIP = 0;
	private int action = SKIP;

	private OrderBy orderBy;

	private Display() {
		this.title = "Quản lý kho hàng";
		this.width = 1280;
		this.height = 720;
		this.init();
	}

	private Display(String title, int width, int height) {
		this.title = title;
		this.width = width;
		this.height = height;
		this.init();
	}

	private void init() {
		this.createView();
		this.handleEvent(this);
	}

	private void createView() {
		this.setTitle(title);
		this.setSize(width, height);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setLayout(new BorderLayout());

		tbrTop = new TopToolBar();
		this.add(tbrTop, BorderLayout.NORTH);

		pnlLeft = new LeftSidePanel(300, height);
		this.add(pnlLeft, BorderLayout.WEST);

		pnlRight = new RightSidePanel();
		this.add(pnlRight, BorderLayout.CENTER);
	}

	private class TopToolBar extends JToolBar {

		private static final long serialVersionUID = 1L;
		private StockView stockView;
		private CategoryView categoryView;
		private ManufacturerView manufacturerView;

		public TopToolBar() {
			this.init();
		}

		private void init() {
			this.createView();
			this.handleEvents();
		}

		private void createView() {
			this.setLayout(new FlowLayout(FlowLayout.LEADING));
			this.setFloatable(false);
			this.setUI(new BasicToolBarUI() {
				@Override
				public void paint(Graphics g, JComponent c) {
					g.setColor(new Color(245, 245, 247));
					g.fillRect(0, 0, c.getWidth(), c.getHeight());
				}
			});

			btnStock = new JButton("Kho hàng");
			btnStock.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/stock.png")));
			this.add(btnStock);

			btnCategory = new JButton("Phân loại");
			btnCategory.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/category.png")));
			this.add(btnCategory);

			btnManufacturer = new JButton("Hãng sản xuất");
			btnManufacturer
					.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/manufacturer.png")));
			this.add(btnManufacturer);
		}

		private void handleEvents() {
			btnStock.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (stockView == null) {
						stockView = new StockView();
					}
					stockView.setVisible(true);
				}
			});

			btnCategory.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (categoryView == null) {
						categoryView = new CategoryView();
					}
					categoryView.setVisible(true);
				}
			});

			btnManufacturer.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (manufacturerView == null) {
						manufacturerView = new ManufacturerView();
					}
					manufacturerView.setVisible(true);
				}
			});
		}

		private class StockView extends JFrame {
			private static final long serialVersionUID = 1L;

			private JButton btnAdd, btnEdit, btnRemove, btnSave, btnSkip, btnSearch;
			private JLabel lblStockId;
			private JTextField txtStockName, txtSearch;
			private JTable tblStocks;
			private DefaultTableModel stockViewTableModel;
			private int stockViewAction = SKIP;
			private int rowIndex = -1;

			private List<Integer> rowIndexes = new ArrayList<>();
			private int position = -1;

			public StockView() {
				this.init();
			}

			private void init() {
				this.createView();
				this.loadData();
				this.handleEvents();
				rowIndex = 0;
				Utils.scrollToVisible(tblStocks, rowIndex, 0);
				showStockDetail(0);
			}

			private void createView() {
				this.setTitle("Quản lý kho hàng");
				this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				this.setMinimumSize(new Dimension(800, 600));
				this.setLocationRelativeTo(null);
				JPanel contentPane = (JPanel) getContentPane();
				contentPane.setLayout(new BorderLayout(0, 10));

				JPanel northPanel = new JPanel(new BorderLayout());

				JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.LEADING));
				buttonGroup.setBorder(BorderFactory.createEtchedBorder());

				btnAdd = new JButton("Thêm");
				btnAdd.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/add.png")));
				buttonGroup.add(btnAdd);

				btnEdit = new JButton("Sửa");
				btnEdit.setEnabled(true);
				btnEdit.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/edit.png")));
				buttonGroup.add(btnEdit);

				btnRemove = new JButton("Xóa");
				btnRemove.setEnabled(true);
				btnRemove.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/remove.png")));
				buttonGroup.add(btnRemove);

				btnSave = new JButton("Lưu");
				btnSave.setEnabled(false);
				btnSave.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/save.png")));
				buttonGroup.add(btnSave);

				btnSkip = new JButton("Bỏ qua");
				btnSkip.setEnabled(false);
				btnSkip.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/skip.png")));
				buttonGroup.add(btnSkip);

				northPanel.add(buttonGroup, BorderLayout.NORTH);

				JPanel form = new JPanel(new GridBagLayout());
				form.setBorder(BorderFactory.createEtchedBorder());

				form.add(new JLabel("ID kho hàng"),
						new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 17, 0, new Insets(5, 5, 5, 5), 5, 5));
				lblStockId = new JLabel();
				form.add(lblStockId, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 17, 0, new Insets(5, 5, 5, 5), 5, 5));

				form.add(new JLabel("Tên kho hàng"),
						new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 17, 0, new Insets(5, 5, 5, 5), 5, 5));
				txtStockName = new JTextField();
				txtStockName.setEditable(false);
				form.add(txtStockName,
						new GridBagConstraints(1, 1, 1, 1, 0.5, 0.0, 17, 2, new Insets(5, 5, 5, 5), 5, 5));

				northPanel.add(form, BorderLayout.CENTER);

				contentPane.add(northPanel, BorderLayout.NORTH);

				JPanel center = new JPanel(new BorderLayout());
				JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEADING));

				searchBox.add(new JLabel("Tìm kiếm"));
				txtSearch = new JTextField(14);

				txtSearch.setPreferredSize(new Dimension(30, 27));
				searchBox.add(txtSearch);

				btnSearch = new JButton("Tìm");
				btnSearch.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/search.png")));
				searchBox.add(btnSearch);

				center.add(searchBox, BorderLayout.NORTH);

				contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));

				String[] columnHeader = { "STT", "Tên kho hàng" };
				stockViewTableModel = new DefaultTableModel(columnHeader, 0) {
					private static final long serialVersionUID = 1L;

					@Override
					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};

				tblStocks = new JTable(stockViewTableModel);
				DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
				cellRenderer.setHorizontalAlignment(JLabel.CENTER);
				tblStocks.getColumnModel().getColumn(0).setMaxWidth(50);
				tblStocks.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
				tblStocks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				tblStocks.setRowHeight(25);
				JScrollPane scrllPane = new JScrollPane(tblStocks);
				center.add(scrllPane, BorderLayout.CENTER);
				contentPane.add(center, BorderLayout.CENTER);
			}

			private void loadData() {
				stockViewTableModel.setRowCount(0);
				stocks.forEach(o -> {
					stockViewTableModel.addRow(new Object[] { stocks.indexOf(o) + 1, o.getName() });
				});
			}

			private void handleEvents() {
				this.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						if (stockViewAction == ADD || stockViewAction == EDIT) {
							int c = JOptionPane.showConfirmDialog(stockView,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								stockView.btnAdd.setEnabled(true);
								stockView.btnEdit.setEnabled(true);
								stockView.btnRemove.setEnabled(true);
								stockView.btnSave.setEnabled(false);
								stockView.btnSkip.setEnabled(false);
								stockView.lockForm(true);
								showStockDetail(rowIndex);
								stockViewAction = SKIP;
							}
						} else {
							stockView.btnAdd.setEnabled(true);
							stockView.btnEdit.setEnabled(true);
							stockView.btnRemove.setEnabled(true);
							stockView.btnSave.setEnabled(false);
							stockView.btnSkip.setEnabled(false);
							stockView.lockForm(true);
							showStockDetail(rowIndex);
							stockViewAction = SKIP;
						}
					}
				});

				this.tblStocks.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) {
						if (stockViewAction != ADD && stockViewAction != EDIT) {
							rowIndex = stockView.tblStocks.getSelectedRow();
							showStockDetail(rowIndex);
						} else {
							int c = JOptionPane.showConfirmDialog(stockView,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								stockView.btnAdd.setEnabled(true);
								stockView.btnEdit.setEnabled(true);
								stockView.btnRemove.setEnabled(true);
								stockView.btnSave.setEnabled(false);
								stockView.btnSkip.setEnabled(false);
								rowIndex = stockView.tblStocks.getSelectedRow();
								showStockDetail(rowIndex);
								lockForm(true);
								stockViewAction = SKIP;
							} else {
								Utils.scrollToVisible(tblStocks, rowIndex, 0);
							}
						}
					}
				});

				this.tblStocks.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN
								|| e.getKeyCode() == KeyEvent.VK_ENTER) {
							rowIndex = stockView.tblStocks.getSelectedRow();
							showStockDetail(rowIndex);
						}
					}
				});

				this.btnAdd.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rowIndex = stockView.tblStocks.getSelectedRow();
						stockView.clearForm();
						stockView.btnAdd.setEnabled(false);
						stockView.btnEdit.setEnabled(false);
						stockView.btnRemove.setEnabled(false);
						stockView.btnSave.setEnabled(true);
						stockView.btnSkip.setEnabled(true);
						stockView.lockForm(false);
						stockView.lblStockId.setText("Auto");
						stockView.txtStockName.requestFocus();
						stockViewAction = ADD;
					}
				});

				this.btnEdit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rowIndex = stockView.tblStocks.getSelectedRow();
						stockView.btnAdd.setEnabled(false);
						stockView.btnEdit.setEnabled(false);
						stockView.btnRemove.setEnabled(false);
						stockView.btnSave.setEnabled(true);
						stockView.btnSkip.setEnabled(true);
						stockView.lockForm(false);
						stockView.txtStockName.requestFocus();
						stockViewAction = EDIT;
					}
				});

				this.btnRemove.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						rowIndex = stockView.tblStocks.getSelectedRow();
						int select = display.tblStocks.getSelectedRow();
						int c = JOptionPane.showConfirmDialog(stockView,
								"Xóa kho hàng sẽ xóa toàn bộ hàng hóa có trong kho. Bạn có chắc chắn muốn xóa?",
								"Xác nhận", JOptionPane.YES_NO_OPTION);
						if (c == JOptionPane.YES_OPTION) {
							Stock s = new Stock();
							String name = stockView.tblStocks.getValueAt(rowIndex, 1) + "";
							try {
								s = Controller.getStockByName(name);
								boolean delete = Controller.deleteStock(s);
								if (delete) {
									for (Stock st : lstStocks) {
										if (st.getId() == stocks.get(rowIndex).getId()) {
											lstStocks.remove(rowIndex);
											break;
										}
									}
									stocks.remove(rowIndex);
									cboStocks.removeAllItems();
									stocks.forEach(o -> {
										cboStocks.addItem(o.getName());
									});
									stockView.loadData();
									refreshTblStock(lstStocks);
									if (rowIndex == stocks.size()) {
										rowIndex--;
									}
									showStockDetail(rowIndex);
									if(chkStocks.isSelected()) {
										if(select == lstStocks.size()) {
											select--;
										}
										Utils.scrollToVisible(display.tblStocks, select, 0);
									} else {
										Utils.scrollToVisible(display.tblStocks, rowIndex, 0);
									}
									handleRowSelectedTblStocks();
								} else {
									JOptionPane.showMessageDialog(stockView,
											"Xóa kho hàng thất bại. Hãy thử lại lần nữa");
								}

							} catch (SQLException e1) {
								e1.printStackTrace();
							}
							stockViewAction = REMOVE;
						}
					}
				});

				this.btnSave.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						switch (stockViewAction) {
						case ADD:
							try {
								Stock s = new Stock();
								String name = txtStockName.getText().trim();
								if (name.isEmpty()) {
									throw new FormValidationException("Bạn chưa nhập vào tên kho hàng!");
								}
								s.setName(name);
								int id = Controller.createStock(s);
								if (id > 0) {
									s.setId(id);
									stocks.add(s);
									stockViewTableModel.addRow(new Object[] { stocks.size(), s.getName() });
									if (!chkStocks.isSelected()) {
										tblStockModel.addRow(new Object[] { stocks.size(), s.getName() });
									}
									cboStocks.addItem(s.getName());
									rowIndex = stocks.size() - 1;
									showStockDetail(rowIndex);
								} else {
									throw new SQLException("Có lỗi xảy ra!");
								}
							} catch (SQLException e1) {
								JOptionPane.showMessageDialog(stockView, "Lỗi server!");
								return;
							} catch (DuplicateValueException | FormValidationException e1) {
								JOptionPane.showMessageDialog(stockView, e1.getMessage());
								txtStockName.requestFocus();
								return;
							} catch (Exception e2) {
								JOptionPane.showMessageDialog(stockView,
										"Có lỗi xảy ra!\nVui lòng liên hệ với nhà phát triển phần mềm để được hỗ trợ!");
								return;
							}
							break;
						case EDIT:
							int row = stockView.tblStocks.getSelectedRow();
							if (row > -1) {
								try {
									Stock s = stocks.get(row);
									String name = txtStockName.getText().trim();
									if (name.isEmpty()) {
										throw new FormValidationException("Bạn chưa nhập vào tên kho hàng!");
									}
									s.setName(name);
									boolean updated = Controller.updateStock(s);
									if (updated) {
										stockViewTableModel.setValueAt(s.getName(), row, 1);
										for (Stock st : lstStocks) {
											if (st.getId() == stocks.get(row).getId()) {
												lstStocks.set(row, s);
												break;
											}
										}
										stocks.set(row, s);
										cboStocks.removeAllItems();
										stocks.forEach(o -> {
											cboStocks.addItem(o.getName());
										});
										refreshTblStock(lstStocks);
									} else {
										throw new SQLException("Có lỗi xảy ra!");
									}
								} catch (SQLException e1) {
									JOptionPane.showMessageDialog(stockView, "Lỗi server!");
									return;
								} catch (DuplicateValueException | FormValidationException e1) {
									JOptionPane.showMessageDialog(stockView, e1.getMessage());
									txtStockName.requestFocus();
									return;
								} catch (Exception e2) {
									JOptionPane.showMessageDialog(stockView,
											"Có lỗi xảy ra!\nVui lòng liên hệ với nhà phát triển phần mềm để được hỗ trợ!");
									return;
								}
							}
							break;
						}
						stockView.btnAdd.setEnabled(true);
						stockView.btnEdit.setEnabled(true);
						stockView.btnRemove.setEnabled(true);
						stockView.btnSave.setEnabled(false);
						stockView.btnSkip.setEnabled(false);
						stockView.lockForm(true);
						stockViewAction = SAVE;
					}
				});

				this.btnSkip.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						stockView.btnAdd.setEnabled(true);
						stockView.btnEdit.setEnabled(true);
						stockView.btnRemove.setEnabled(true);
						stockView.btnSave.setEnabled(false);
						stockView.btnSkip.setEnabled(false);
						stockView.lockForm(true);
						if (rowIndex > -1) {
							showStockDetail(rowIndex);
						} else {
							clearForm();
						}
						stockViewAction = SKIP;
					}
				});

				this.txtSearch.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							if (rowIndexes.size() > 0) {
								if (position > rowIndexes.size() - 1) {
									position = 0;
								}
								int rowIndex = rowIndexes.get(position);
								Utils.scrollToVisible(stockView.tblStocks, rowIndex, 0);
								showStockDetail(rowIndex);
								position++;
							}
						} else {
							String text = txtSearch.getText();
							rowIndexes.clear();
							if (text.length() > 0) {
								boolean found = false;
								for (Stock s : stocks) {
									if (s.getName().toLowerCase().contains(text.toLowerCase())) {
										rowIndexes.add(stocks.indexOf(s));
										found = true;
									}
								}
								if (!found) {
									stockView.clearForm();
								} else {
									position = 0;
									int rowIndex = rowIndexes.get(position);
									Utils.scrollToVisible(stockView.tblStocks, rowIndex, 0);
									showStockDetail(rowIndex);
									position++;
								}
							} else {
								stockView.clearForm();
							}
						}
					}
				});

				this.txtSearch.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseReleased(MouseEvent e) {
						if (stockViewAction == ADD || stockViewAction == EDIT) {
							int c = JOptionPane.showConfirmDialog(categoryView,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								btnAdd.setEnabled(true);
								btnEdit.setEnabled(true);
								btnRemove.setEnabled(true);
								btnSave.setEnabled(false);
								btnSkip.setEnabled(false);
								stockViewAction = SKIP;
								setFormLock(true);
								rowIndex = stockView.tblStocks.getSelectedRow();
								showStockDetail(rowIndex);
							}
						} else {
							showStockDetail(rowIndex);
						}
					}

				});

				this.btnSearch.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (rowIndexes.size() > 0) {
							if (position > rowIndexes.size() - 1) {
								position = 0;
							}
							int rowIndex = rowIndexes.get(position);
							Utils.scrollToVisible(stockView.tblStocks, rowIndex, 0);
							showStockDetail(rowIndex);
							position++;
						}
					}
				});
			}

			private void lockForm(boolean lock) {
				txtStockName.setEditable(!lock);
			}

			private void clearForm() {
				lblStockId.setText(null);
				txtStockName.setText(null);
				stockView.tblStocks.clearSelection();
				btnEdit.setEnabled(false);
				btnRemove.setEnabled(false);
			}

			private void showStockDetail(int rowIndex) {
				if (rowIndex > -1) {
					Utils.scrollToVisible(tblStocks, rowIndex, 0);
					Stock s = stocks.get(rowIndex);
					lblStockId.setText(String.valueOf(s.getId()));
					txtStockName.setText(s.getName());
					if (stockViewAction != ADD && stockViewAction != EDIT) {
						btnEdit.setEnabled(true);
						btnRemove.setEnabled(true);
					}
				} else {
					stockView.clearForm();
					txtStockName.setEditable(false);
				}
			}
		}

		private class CategoryView extends JFrame {
			private static final long serialVersionUID = 1L;

			private JButton btnAdd, btnEdit, btnRemove, btnSave, btnSkip, btnSearch;
			private JLabel lblCategoryId;
			private JTextField txtCategoryName, txtSearch;
			private JTable tblCategories;
			private DefaultTableModel categoryViewTableModel;
			private int categoryViewAction = SKIP;
			private int rowIndex = -1;

			private List<Integer> rowIndexes = new ArrayList<>();
			private int position = -1;

			public CategoryView() {
				this.init();
			}

			private void init() {
				this.createView();
				this.loadData();
				showCategoryDetail(0);
				this.handleEvents();
			}

			private void createView() {
				this.setTitle("Quản lý loại hàng");
				this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				this.setMinimumSize(new Dimension(800, 600));
				this.setLocationRelativeTo(null);
				JPanel contentPane = (JPanel) getContentPane();
				contentPane.setLayout(new BorderLayout(0, 10));

				JPanel northPanel = new JPanel(new BorderLayout());

				JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.LEADING));
				buttonGroup.setBorder(BorderFactory.createEtchedBorder());

				btnAdd = new JButton("Thêm");
				btnAdd.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/add.png")));
				buttonGroup.add(btnAdd);

				btnEdit = new JButton("Sửa");
				btnEdit.setEnabled(false);
				btnEdit.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/edit.png")));
				buttonGroup.add(btnEdit);

				btnRemove = new JButton("Xóa");
				btnRemove.setEnabled(false);
				btnRemove.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/remove.png")));
				buttonGroup.add(btnRemove);

				btnSave = new JButton("Lưu");
				btnSave.setEnabled(false);
				btnSave.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/save.png")));
				buttonGroup.add(btnSave);

				btnSkip = new JButton("Bỏ qua");
				btnSkip.setEnabled(false);
				btnSkip.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/skip.png")));
				buttonGroup.add(btnSkip);

				northPanel.add(buttonGroup, BorderLayout.NORTH);

				JPanel form = new JPanel(new GridBagLayout());
				form.setBorder(BorderFactory.createEtchedBorder());

				form.add(new JLabel("ID Loại hàng"),
						new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 17, 0, new Insets(5, 5, 5, 5), 5, 5));
				lblCategoryId = new JLabel();
				form.add(lblCategoryId,
						new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 17, 0, new Insets(5, 5, 5, 5), 5, 5));

				form.add(new JLabel("Tên loại hàng"),
						new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 17, 0, new Insets(5, 5, 5, 5), 5, 5));
				txtCategoryName = new JTextField();
				txtCategoryName.setEditable(false);
				form.add(txtCategoryName,
						new GridBagConstraints(1, 1, 1, 1, 0.5, 0.0, 17, 2, new Insets(5, 5, 5, 5), 5, 5));

				northPanel.add(form, BorderLayout.CENTER);
				contentPane.add(northPanel, BorderLayout.NORTH);

				JPanel center = new JPanel(new BorderLayout());
				JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEADING));

				searchBox.add(new JLabel("Tìm kiếm"));
				txtSearch = new JTextField(14);

				txtSearch.setPreferredSize(new Dimension(30, 27));
				searchBox.add(txtSearch);

				btnSearch = new JButton("Tìm");
				btnSearch.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/search.png")));
				searchBox.add(btnSearch);

				center.add(searchBox, BorderLayout.NORTH);

				contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));

				String[] columnHeader = { "#", "Tên loại hàng" };
				categoryViewTableModel = new DefaultTableModel(columnHeader, 0) {
					private static final long serialVersionUID = 1L;

					@Override
					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};

				tblCategories = new JTable(categoryViewTableModel);
				DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
				cellRenderer.setHorizontalAlignment(JLabel.CENTER);
				tblCategories.getColumnModel().getColumn(0).setMaxWidth(50);
				tblCategories.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
				tblCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				tblCategories.setRowHeight(25);
				JScrollPane scrllPane = new JScrollPane(tblCategories);
				center.add(scrllPane, BorderLayout.CENTER);
				contentPane.add(center, BorderLayout.CENTER);
			}

			private void loadData() {
				categoryViewTableModel.setRowCount(0);
				categories.forEach(o -> {
					categoryViewTableModel.addRow(new Object[] { categories.indexOf(o) + 1, o.getName() });
				});
			}

			private void handleEvents() {
				this.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						if (categoryViewAction == ADD || categoryViewAction == EDIT) {
							int c = JOptionPane.showConfirmDialog(categoryView,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								categoryView.btnAdd.setEnabled(true);
								categoryView.btnEdit.setEnabled(true);
								categoryView.btnRemove.setEnabled(true);
								categoryView.btnSave.setEnabled(false);
								categoryView.btnSkip.setEnabled(false);
								categoryView.lockForm(true);
								showCategoryDetail(rowIndex);
								categoryViewAction = SKIP;
							}
						} else {
							categoryView.btnAdd.setEnabled(true);
							categoryView.btnEdit.setEnabled(true);
							categoryView.btnRemove.setEnabled(true);
							categoryView.btnSave.setEnabled(false);
							categoryView.btnSkip.setEnabled(false);
							categoryView.lockForm(true);
							showCategoryDetail(rowIndex);
							categoryViewAction = SKIP;
						}
					}
				});

				this.tblCategories.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) {
						if (categoryViewAction == ADD || categoryViewAction == EDIT) {
							int c = JOptionPane.showConfirmDialog(categoryView,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								rowIndex = categoryView.tblCategories.getSelectedRow();
								btnAdd.setEnabled(true);
								btnEdit.setEnabled(true);
								btnRemove.setEnabled(true);
								btnSave.setEnabled(false);
								btnSkip.setEnabled(false);
								lockForm(true);
								showCategoryDetail(rowIndex);
								categoryViewAction = SKIP;
							} else {
								Utils.scrollToVisible(tblCategories, rowIndex, 0);
							}
						} else {
							rowIndex = categoryView.tblCategories.getSelectedRow();
							showCategoryDetail(rowIndex);
						}
					}
				});

				this.tblCategories.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN
								|| e.getKeyCode() == KeyEvent.VK_ENTER) {
							rowIndex = categoryView.tblCategories.getSelectedRow();
							showCategoryDetail(rowIndex);
						}
					}
				});

				this.btnAdd.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rowIndex = categoryView.tblCategories.getSelectedRow();
						categoryView.clearForm();
						categoryView.btnAdd.setEnabled(false);
						categoryView.btnEdit.setEnabled(false);
						categoryView.btnRemove.setEnabled(false);
						categoryView.btnSave.setEnabled(true);
						categoryView.btnSkip.setEnabled(true);
						categoryView.lockForm(false);
						categoryView.lblCategoryId.setText("Auto");
						categoryView.txtCategoryName.requestFocus();
						categoryViewAction = ADD;
					}
				});

				this.btnEdit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rowIndex = categoryView.tblCategories.getSelectedRow();
						categoryView.btnAdd.setEnabled(false);
						categoryView.btnEdit.setEnabled(false);
						categoryView.btnRemove.setEnabled(false);
						categoryView.btnSave.setEnabled(true);
						categoryView.btnSkip.setEnabled(true);
						categoryView.lockForm(false);
						categoryView.txtCategoryName.requestFocus();
						categoryViewAction = EDIT;
					}
				});

				this.btnRemove.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						rowIndex = tblCategories.getSelectedRow();
						int c = JOptionPane.showConfirmDialog(categoryView,
								"Xóa loại hàng sẽ xóa toàn bộ hàng hóa có loại đó. Bạn có chắc chắn muốn xóa?",
								"Xác nhận", JOptionPane.YES_NO_OPTION);
						if (c == JOptionPane.YES_OPTION) {
							Category category = new Category();
							String name = tblCategories.getValueAt(rowIndex, 1) + "";
							try {
								category = Controller.getCategoryByName(name);
								boolean delete = Controller.deleteCategory(category);
								if (delete) {
									categories.remove(rowIndex);
									cboCategories.removeAllItems();
									categories.forEach(o -> {
										cboCategories.addItem(o.getName());
									});
									categoryView.loadData();
									if (rowIndex == categories.size()) {
										rowIndex--;
									}
									showCategoryDetail(rowIndex);
									handleRowSelectedTblStocks();
								} else {
									JOptionPane.showMessageDialog(categoryView,
											"Xóa loại hàng thất bại. Hãy thử lại lần nữa");
								}

							} catch (SQLException e1) {
								e1.printStackTrace();
							}
							categoryViewAction = REMOVE;
						}
					}
				});

				this.btnSave.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						switch (categoryViewAction) {
						case ADD:
							try {
								Category c = new Category();
								String name = txtCategoryName.getText().trim();
								if (name.isEmpty()) {
									throw new FormValidationException("Bạn chưa nhập vào tên loại hàng!");
								}
								c.setName(name);
								int id = Controller.createCategory(c);
								if (id > 0) {
									c.setId(id);
									categories.add(c);
									categoryViewTableModel.addRow(new Object[] { categories.size(), c.getName() });
									cboCategories.addItem(c.getName());
									rowIndex = categories.size() - 1;
									showCategoryDetail(rowIndex);
								} else {
									throw new SQLException("Có lỗi xảy ra!");
								}
							} catch (SQLException e1) {
								JOptionPane.showMessageDialog(categoryView, "Lỗi server!");
								return;
							} catch (DuplicateValueException | FormValidationException e1) {
								JOptionPane.showMessageDialog(categoryView, e1.getMessage());
								txtCategoryName.requestFocus();
								return;
							} catch (Exception e2) {
								JOptionPane.showMessageDialog(categoryView,
										"Có lỗi xảy ra!\nVui lòng liên hệ với nhà phát triển phần mềm để được hỗ trợ!");
								return;
							}
							break;
						case EDIT:
							int row = categoryView.tblCategories.getSelectedRow();
							if (row > -1) {
								try {
									Category c = categories.get(row);
									String name = txtCategoryName.getText().trim();
									if (name.isEmpty()) {
										throw new FormValidationException("Bạn chưa nhập vào tên kho hàng!");
									}
									c.setName(name);
									boolean updated = Controller.updateCategory(c);
									if (updated) {
										categories.set(row, c);
										categoryViewTableModel.setValueAt(c.getName(), row, 1);
										cboCategories.removeAllItems();
										categories.forEach(o -> {
											cboCategories.addItem(o.getName());
										});
									} else {
										throw new SQLException("Có lỗi xảy ra!");
									}
								} catch (SQLException e1) {
									JOptionPane.showMessageDialog(categoryView, "Lỗi server!");
									return;
								} catch (DuplicateValueException | FormValidationException e1) {
									JOptionPane.showMessageDialog(categoryView, e1.getMessage());
									txtCategoryName.requestFocus();
									return;
								} catch (Exception e2) {
									JOptionPane.showMessageDialog(categoryView,
											"Có lỗi xảy ra!\nVui lòng liên hệ với nhà phát triển phần mềm để được hỗ trợ!");
									return;
								}
							}
							break;
						}
						categoryView.btnAdd.setEnabled(true);
						categoryView.btnEdit.setEnabled(true);
						categoryView.btnRemove.setEnabled(true);
						categoryView.btnSave.setEnabled(false);
						categoryView.btnSkip.setEnabled(false);
						categoryView.lockForm(true);
						categoryViewAction = SAVE;
					}
				});

				this.btnSkip.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						categoryView.btnAdd.setEnabled(true);
						categoryView.btnEdit.setEnabled(true);
						categoryView.btnRemove.setEnabled(true);
						categoryView.btnSave.setEnabled(false);
						categoryView.btnSkip.setEnabled(false);
						categoryView.lockForm(true);
						showCategoryDetail(rowIndex);
						categoryViewAction = SKIP;
					}
				});

				this.txtSearch.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							if (rowIndexes.size() > 0) {
								if (position > rowIndexes.size() - 1) {
									position = 0;
								}
								int rowIndex = rowIndexes.get(position);
								Utils.scrollToVisible(categoryView.tblCategories, rowIndex, 0);
								showCategoryDetail(rowIndex);
								position++;
							}
						} else {
							String text = txtSearch.getText();
							rowIndexes.clear();
							if (text.length() > 0) {
								boolean found = false;
								for (Category c : categories) {
									if (c.getName().toLowerCase().contains(text.toLowerCase())) {
										rowIndexes.add(categories.indexOf(c));
										found = true;
									}
								}
								if (!found) {
									categoryView.clearForm();
								} else {
									position = 0;
									int rowIndex = rowIndexes.get(position);
									Utils.scrollToVisible(categoryView.tblCategories, rowIndex, 0);
									showCategoryDetail(rowIndex);
									position++;
								}
							} else {
								categoryView.clearForm();
							}
						}
					}
				});

				this.txtSearch.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseReleased(MouseEvent e) {
						if (categoryViewAction == ADD || categoryViewAction == EDIT) {
							int c = JOptionPane.showConfirmDialog(categoryView,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								btnAdd.setEnabled(true);
								btnEdit.setEnabled(true);
								btnRemove.setEnabled(true);
								btnSave.setEnabled(false);
								btnSkip.setEnabled(false);
								action = SKIP;
								setFormLock(true);
							}
						}
					}
				});

				this.btnSearch.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (categoryViewAction == ADD || categoryViewAction == EDIT) {
							int c = JOptionPane.showConfirmDialog(categoryView,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								btnAdd.setEnabled(true);
								btnEdit.setEnabled(true);
								btnRemove.setEnabled(true);
								btnSave.setEnabled(false);
								btnSkip.setEnabled(false);
								action = SKIP;
								setFormLock(true);
								rowIndex = categoryView.tblCategories.getSelectedRow();
								showCategoryDetail(rowIndex);
								if (rowIndexes.size() > 0) {
									if (position > rowIndexes.size() - 1) {
										position = 0;
									}
									int rowIndex = rowIndexes.get(position);
									Utils.scrollToVisible(categoryView.tblCategories, rowIndex, 0);
									showCategoryDetail(rowIndex);
									position++;
								}
							}
						}
					}
				});
			}

			private void lockForm(boolean lock) {
				txtCategoryName.setEditable(!lock);
			}

			private void clearForm() {
				lblCategoryId.setText(null);
				txtCategoryName.setText(null);
				categoryView.tblCategories.clearSelection();
				btnEdit.setEnabled(false);
				btnRemove.setEnabled(false);
			}

			private void showCategoryDetail(int rowIndex) {
				if (rowIndex > -1) {
					Utils.scrollToVisible(tblCategories, rowIndex, 0);
					Category c = categories.get(rowIndex);
					lblCategoryId.setText(String.valueOf(c.getId()));
					txtCategoryName.setText(c.getName());
					if (categoryViewAction != ADD && categoryViewAction != EDIT) {
						btnEdit.setEnabled(true);
						btnRemove.setEnabled(true);
					}
				} else {
					categoryView.clearForm();
					txtCategoryName.setEditable(false);

				}
			}
		}

		private class ManufacturerView extends JFrame {
			private static final long serialVersionUID = 1L;

			private JButton btnAdd, btnEdit, btnRemove, btnSave, btnSkip, btnSearch;
			private JLabel lblManufacturerId;
			private JTextField txtManufacturerName, txtSearch;
			private JTable tblManufacturers;
			private DefaultTableModel manufacturerViewTableModel;
			private int manufacturerViewAction = SKIP;
			private int rowIndex = -1;

			private List<Integer> rowIndexes = new ArrayList<>();
			private int position = -1;

			public ManufacturerView() {
				this.init();
			}

			private void init() {
				this.createView();
				this.loadData();
				this.handleEvents();
				rowIndex = 0;
				showManufacturerDetail(rowIndex);
			}

			private void createView() {
				this.setTitle("Quản lý hãng sản xuất");
				this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				this.setMinimumSize(new Dimension(800, 600));
				this.setLocationRelativeTo(null);
				JPanel contentPane = (JPanel) getContentPane();
				contentPane.setLayout(new BorderLayout(0, 10));

				JPanel northPanel = new JPanel(new BorderLayout());

				JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.LEADING));
				buttonGroup.setBorder(BorderFactory.createEtchedBorder());

				btnAdd = new JButton("Thêm");
				btnAdd.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/add.png")));
				buttonGroup.add(btnAdd);

				btnEdit = new JButton("Sửa");
				btnEdit.setEnabled(false);
				btnEdit.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/edit.png")));
				buttonGroup.add(btnEdit);

				btnRemove = new JButton("Xóa");
				btnRemove.setEnabled(false);
				btnRemove.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/remove.png")));
				buttonGroup.add(btnRemove);

				btnSave = new JButton("Lưu");
				btnSave.setEnabled(false);
				btnSave.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/save.png")));
				buttonGroup.add(btnSave);

				btnSkip = new JButton("Bỏ qua");
				btnSkip.setEnabled(false);
				btnSkip.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/skip.png")));
				buttonGroup.add(btnSkip);

				northPanel.add(buttonGroup, BorderLayout.NORTH);

				JPanel form = new JPanel(new GridBagLayout());
				form.setBorder(BorderFactory.createEtchedBorder());

				form.add(new JLabel("ID hãng sản xuất"),
						new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 17, 0, new Insets(5, 5, 5, 5), 5, 5));
				lblManufacturerId = new JLabel();
				form.add(lblManufacturerId,
						new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 17, 0, new Insets(5, 5, 5, 5), 5, 5));

				form.add(new JLabel("Tên hãng sản xuất"),
						new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 17, 0, new Insets(5, 5, 5, 5), 5, 5));
				txtManufacturerName = new JTextField();
				txtManufacturerName.setEditable(false);
				form.add(txtManufacturerName,
						new GridBagConstraints(1, 1, 1, 1, 0.5, 0.0, 17, 2, new Insets(5, 5, 5, 5), 5, 5));

				northPanel.add(form, BorderLayout.CENTER);

				contentPane.add(northPanel, BorderLayout.NORTH);

				JPanel center = new JPanel(new BorderLayout());
				JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEADING));

				searchBox.add(new JLabel("Tìm kiếm"));
				txtSearch = new JTextField(14);

				txtSearch.setPreferredSize(new Dimension(30, 27));
				searchBox.add(txtSearch);

				btnSearch = new JButton("Tìm");
				btnSearch.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/search.png")));
				searchBox.add(btnSearch);

				center.add(searchBox, BorderLayout.NORTH);

				contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));

				String[] columnHeader = { "STT", "Tên hãng sản xuất" };
				manufacturerViewTableModel = new DefaultTableModel(columnHeader, 0) {
					private static final long serialVersionUID = 1L;

					@Override
					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};

				tblManufacturers = new JTable(manufacturerViewTableModel);
				tblManufacturers.setRowHeight(25);
				DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
				cellRenderer.setHorizontalAlignment(JLabel.CENTER);
				tblManufacturers.getColumnModel().getColumn(0).setMaxWidth(50);
				tblManufacturers.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
				JScrollPane scrllPane = new JScrollPane(tblManufacturers);
				center.add(scrllPane, BorderLayout.CENTER);
				contentPane.add(center, BorderLayout.CENTER);
			}

			private void loadData() {
				manufacturerViewTableModel.setRowCount(0);
				manufacturers.forEach(o -> {
					manufacturerViewTableModel.addRow(new Object[] { manufacturers.indexOf(o) + 1, o.getName() });
				});
			}

			private void handleEvents() {
				this.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						if (manufacturerViewAction == ADD || manufacturerViewAction == EDIT) {
							int c = JOptionPane.showConfirmDialog(manufacturerView,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								manufacturerView.btnAdd.setEnabled(true);
								manufacturerView.btnEdit.setEnabled(true);
								manufacturerView.btnRemove.setEnabled(true);
								manufacturerView.btnSave.setEnabled(false);
								manufacturerView.btnSkip.setEnabled(false);
								manufacturerView.lockForm(true);
								showManufacturerDetail(rowIndex);
								manufacturerViewAction = SKIP;
							}
						} else {
							manufacturerView.btnAdd.setEnabled(true);
							manufacturerView.btnEdit.setEnabled(true);
							manufacturerView.btnRemove.setEnabled(true);
							manufacturerView.btnSave.setEnabled(false);
							manufacturerView.btnSkip.setEnabled(false);
							manufacturerView.lockForm(true);
							showManufacturerDetail(rowIndex);
							manufacturerViewAction = SKIP;
						}
					}
				});

				this.tblManufacturers.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) {

						if (manufacturerViewAction != ADD && manufacturerViewAction != EDIT) {
							rowIndex = manufacturerView.tblManufacturers.getSelectedRow();
							showManufacturerDetail(rowIndex);
						} else {
							int c = JOptionPane.showConfirmDialog(categoryView,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								btnAdd.setEnabled(true);
								btnEdit.setEnabled(true);
								btnRemove.setEnabled(true);
								btnSave.setEnabled(false);
								btnSkip.setEnabled(false);
								manufacturerViewAction = SKIP;
								lockForm(true);
								rowIndex = manufacturerView.tblManufacturers.getSelectedRow();
								showManufacturerDetail(rowIndex);
							} else {
								Utils.scrollToVisible(tblManufacturers, rowIndex, 0);
							}
						}
					}
				});

				this.tblManufacturers.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN
								|| e.getKeyCode() == KeyEvent.VK_ENTER) {
							rowIndex = manufacturerView.tblManufacturers.getSelectedRow();
							showManufacturerDetail(rowIndex);
						}
					}
				});

				this.btnAdd.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rowIndex = manufacturerView.tblManufacturers.getSelectedRow();
						manufacturerView.clearForm();
						manufacturerView.btnAdd.setEnabled(false);
						manufacturerView.btnEdit.setEnabled(false);
						manufacturerView.btnRemove.setEnabled(false);
						manufacturerView.btnSave.setEnabled(true);
						manufacturerView.btnSkip.setEnabled(true);
						manufacturerView.lockForm(false);
						manufacturerView.lblManufacturerId.setText("Auto");
						manufacturerView.txtManufacturerName.requestFocus();
						manufacturerViewAction = ADD;
					}
				});

				this.btnEdit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rowIndex = manufacturerView.tblManufacturers.getSelectedRow();
						manufacturerView.btnAdd.setEnabled(false);
						manufacturerView.btnEdit.setEnabled(false);
						manufacturerView.btnRemove.setEnabled(false);
						manufacturerView.btnSave.setEnabled(true);
						manufacturerView.btnSkip.setEnabled(true);
						manufacturerView.lockForm(false);
						manufacturerView.txtManufacturerName.requestFocus();
						manufacturerViewAction = EDIT;
					}
				});

				this.btnRemove.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						rowIndex = tblManufacturers.getSelectedRow();
						int c = JOptionPane.showConfirmDialog(manufacturerView,
								"Xóa loại hàng sẽ xóa toàn bộ hàng hóa có loại đó. Bạn có chắc chắn muốn xóa?",
								"Xác nhận", JOptionPane.YES_NO_OPTION);
						if (c == JOptionPane.YES_OPTION) {
							Manufacturer manufacturer = new Manufacturer();
							String name = tblManufacturers.getValueAt(rowIndex, 1) + "";
							try {
								manufacturer = Controller.getManufacturerByName(name);
								boolean delete = Controller.deleteManufacturer(manufacturer);
								if (delete) {
									manufacturers.remove(rowIndex);
									cboManufacturers.removeAllItems();
									manufacturers.forEach(o -> {
										cboManufacturers.addItem(o.getName());
									});
									manufacturerView.loadData();
									if (rowIndex == categories.size()) {
										rowIndex--;
									}
									showManufacturerDetail(rowIndex);
									handleRowSelectedTblStocks();
								} else {
									JOptionPane.showMessageDialog(manufacturerView,
											"Xóa hãng sản xuất thất bại. Hãy thử lại lần nữa");
								}

							} catch (SQLException e1) {
								e1.printStackTrace();
							}
							manufacturerViewAction = REMOVE;
						}
					}
				});

				this.btnSave.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						switch (manufacturerViewAction) {
						case ADD:
							try {
								Manufacturer c = new Manufacturer();
								String name = txtManufacturerName.getText().trim();
								if (name.isEmpty()) {
									throw new FormValidationException("Bạn chưa nhập vào tên hãng sản xuất!");
								}
								c.setName(name);
								int id = Controller.createManufacturer(c);
								if (id > 0) {
									c.setId(id);
									manufacturers.add(c);
									manufacturerViewTableModel
											.addRow(new Object[] { manufacturers.size(), c.getName() });
									cboManufacturers.addItem(c.getName());
									rowIndex = manufacturers.size() - 1;
									showManufacturerDetail(rowIndex);
								} else {
									throw new SQLException("Có lỗi xảy ra!");
								}
							} catch (SQLException e1) {
								JOptionPane.showMessageDialog(manufacturerView, "Lỗi server!");
								return;
							} catch (DuplicateValueException | FormValidationException e1) {
								JOptionPane.showMessageDialog(manufacturerView, e1.getMessage());
								txtManufacturerName.requestFocus();
								return;
							} catch (Exception e2) {
								JOptionPane.showMessageDialog(manufacturerView,
										"Có lỗi xảy ra!\nVui lòng liên hệ với nhà phát triển phần mềm để được hỗ trợ!");
								return;
							}
							break;
						case EDIT:
							int row = manufacturerView.tblManufacturers.getSelectedRow();
							if (row > -1) {
								try {
									Manufacturer m = manufacturers.get(row);
									String name = txtManufacturerName.getText().trim();
									if (name.isEmpty()) {
										throw new FormValidationException("Bạn chưa nhập vào tên hãng sản xuất!");
									}
									m.setName(name);
									boolean updated = Controller.updateManufacturer(m);
									if (updated) {
										manufacturers.set(row, m);
										manufacturerViewTableModel.setValueAt(m.getName(), row, 1);
										cboManufacturers.removeAllItems();
										manufacturers.forEach(o -> {
											cboManufacturers.addItem(o.getName());
										});
									} else {
										throw new SQLException("Có lỗi xảy ra!");
									}
								} catch (SQLException e1) {
									JOptionPane.showMessageDialog(manufacturerView, "Lỗi server!");
									return;
								} catch (DuplicateValueException | FormValidationException e1) {
									JOptionPane.showMessageDialog(manufacturerView, e1.getMessage());
									txtManufacturerName.requestFocus();
									return;
								} catch (Exception e2) {
									JOptionPane.showMessageDialog(manufacturerView,
											"Có lỗi xảy ra!\nVui lòng liên hệ với nhà phát triển phần mềm để được hỗ trợ!");
									return;
								}
							}
							break;
						}
						manufacturerView.btnAdd.setEnabled(true);
						manufacturerView.btnEdit.setEnabled(true);
						manufacturerView.btnRemove.setEnabled(true);
						manufacturerView.btnSave.setEnabled(false);
						manufacturerView.btnSkip.setEnabled(false);
						manufacturerView.lockForm(true);
						manufacturerViewAction = SAVE;
					}
				});

				this.btnSkip.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						manufacturerView.btnAdd.setEnabled(true);
						manufacturerView.btnEdit.setEnabled(true);
						manufacturerView.btnRemove.setEnabled(true);
						manufacturerView.btnSave.setEnabled(false);
						manufacturerView.btnSkip.setEnabled(false);
						manufacturerView.lockForm(true);
						if (rowIndex > -1) {
							showManufacturerDetail(rowIndex);
						} else {
							clearForm();
						}
						manufacturerViewAction = SKIP;
					}
				});

				this.txtSearch.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							if (rowIndexes.size() > 0) {
								if (position > rowIndexes.size() - 1) {
									position = 0;
								}
								int rowIndex = rowIndexes.get(position);
								Utils.scrollToVisible(manufacturerView.tblManufacturers, rowIndex, 0);
								showManufacturerDetail(rowIndex);
								position++;
							}
						} else {
							String text = txtSearch.getText();
							rowIndexes.clear();
							if (text.length() > 0) {
								boolean found = false;
								for (Manufacturer m : manufacturers) {
									if (m.getName().toLowerCase().contains(text.toLowerCase())) {
										rowIndexes.add(manufacturers.indexOf(m));
										found = true;
									}
								}
								if (!found) {
									manufacturerView.clearForm();
								} else {
									position = 0;
									int rowIndex = rowIndexes.get(position);
									Utils.scrollToVisible(manufacturerView.tblManufacturers, rowIndex, 0);
									showManufacturerDetail(rowIndex);
									position++;
								}
							} else {
								manufacturerView.clearForm();
							}
						}
					}
				});

				this.btnSearch.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (rowIndexes.size() > 0) {
							if (position > rowIndexes.size() - 1) {
								position = 0;
							}
							int rowIndex = rowIndexes.get(position);
							Utils.scrollToVisible(manufacturerView.tblManufacturers, rowIndex, 0);
							showManufacturerDetail(rowIndex);
							position++;
						}
					}
				});
			}

			private void lockForm(boolean lock) {
				txtManufacturerName.setEditable(!lock);
			}

			private void clearForm() {
				lblManufacturerId.setText(null);
				txtManufacturerName.setText(null);
				manufacturerView.tblManufacturers.clearSelection();
				btnEdit.setEnabled(false);
				btnRemove.setEnabled(false);
			}

			private void showManufacturerDetail(int rowIndex) {
				if (rowIndex > -1) {
					Utils.scrollToVisible(tblManufacturers, rowIndex, 0);
					Manufacturer c = manufacturers.get(rowIndex);
					lblManufacturerId.setText(String.valueOf(c.getId()));
					txtManufacturerName.setText(c.getName());
					if (manufacturerViewAction != ADD && manufacturerViewAction != EDIT) {
						btnEdit.setEnabled(true);
						btnRemove.setEnabled(true);
					}
				} else {
					manufacturerView.clearForm();
					txtManufacturerName.setEditable(false);
				}
			}
		}

	}

	private class LeftSidePanel extends JPanel {

		private static final long serialVersionUID = 1L;

		private int width, height;

		public LeftSidePanel(int width, int height) {
			this.width = width;
			this.height = height;
			this.init();
		}

		private void init() {
			this.createView();
			loadStock(true);
			this.handleEvents();
		}

		private void createView() {
			this.setLayout(new BorderLayout());
			this.setBorder(new EmptyBorder(10, 10, 10, 7));
			this.setPreferredSize(new Dimension(width, height));

			JPanel pnlCheckBox = new JPanel(new FlowLayout(FlowLayout.LEFT));
			chkStocks = new JCheckBox("Chỉ hiển thị kho có hàng");
			chkStocks.setSelected(true);
			pnlCheckBox.add(chkStocks);
			this.add(pnlCheckBox, BorderLayout.NORTH);

			JPanel pnlStocks = new JPanel(new BorderLayout());
			pnlStocks.setBorder(new EmptyBorder(0, 7, 0, 7));

			String[] columnNames = { "STT", "Tên kho hàng" };
			tblStockModel = new DefaultTableModel(columnNames, 0) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};

			tblStocks = new JTable(tblStockModel);
			tblStocks.getColumnModel().getColumn(0).setMaxWidth(50);
			tblStocks.getTableHeader().setReorderingAllowed(false);
			tblStocks.setRowHeight(28);
			tblStocks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			JScrollPane scrllStocks = new JScrollPane(tblStocks);
			pnlStocks.add(scrllStocks, BorderLayout.CENTER);
			this.add(pnlStocks, BorderLayout.CENTER);
		}

		private void handleEvents() {
			chkStocks.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (action == ADD || action == EDIT) {
						int c = JOptionPane.showConfirmDialog(display,
								"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
								JOptionPane.YES_NO_OPTION);
						if (c == JOptionPane.YES_OPTION) {
							btnAdd.setEnabled(true);
							btnEdit.setEnabled(true);
							btnRemove.setEnabled(true);
							btnSave.setEnabled(false);
							btnSkip.setEnabled(false);
							action = SKIP;
							setFormLock(true);
							if (chkStocks.isSelected()) {
								loadStock(true);
							} else {
								loadStock(false);
							}
							handleRowSelectedTblStocks();
						}
					} else {
						if (chkStocks.isSelected()) {
							loadStock(true);
							handleRowSelectedTblStocks();
						} else {
							loadStock(false);
							handleRowSelectedTblStocks();
						}
					}

				}
			});

			tblStocks.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					if (action == ADD || action == EDIT) {
						int c = JOptionPane.showConfirmDialog(display,
								"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
								JOptionPane.YES_NO_OPTION);
						if (c == JOptionPane.YES_OPTION) {
							btnAdd.setEnabled(true);
							btnEdit.setEnabled(true);
							btnRemove.setEnabled(true);
							btnSave.setEnabled(false);
							btnSkip.setEnabled(false);
							action = SKIP;
							setFormLock(true);
							handleRowSelectedTblStocks();
						}
					} else {
						handleRowSelectedTblStocks();
					}
				}
			});

			tblStocks.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN
							|| e.getKeyCode() == KeyEvent.VK_ENTER) {
						handleRowSelectedTblStocks();
					}
				}
			});
		}
	}

	private class RightSidePanel extends JPanel {
		private static final long serialVersionUID = 1L;

		private ButtonGroupPanel pnlButtonGroup;
		private FormInputPanel pnlFormInput;
		private FormSeachGroupPanel pnlFormSearchGroup;
		private GoodsPanel pnlGoods;

		public RightSidePanel() {
			this.init();
		}

		private void init() {
			this.createView();
			setFormLock(true);
		}

		private void createView() {
			this.setLayout(new BorderLayout(0, 20));
			this.setBorder(new EmptyBorder(10, 5, 10, 10));

			// NORTH
			JPanel pnlNorth = new JPanel(new BorderLayout());

			pnlButtonGroup = new ButtonGroupPanel();
			pnlNorth.add(pnlButtonGroup, BorderLayout.NORTH);

			pnlFormInput = new FormInputPanel();
			pnlNorth.add(pnlFormInput, BorderLayout.CENTER);

			this.add(pnlNorth, BorderLayout.NORTH);

			// CENTER
			JPanel pnlCenter = new JPanel(new BorderLayout());

			pnlFormSearchGroup = new FormSeachGroupPanel();
			pnlCenter.add(pnlFormSearchGroup, BorderLayout.NORTH);

			pnlGoods = new GoodsPanel();
			pnlCenter.add(pnlGoods, BorderLayout.CENTER);

			this.add(pnlCenter, BorderLayout.CENTER);
		}

		private class ButtonGroupPanel extends JPanel {
			private static final long serialVersionUID = 1L;

			private int rowSelectedIndex = -1;

			public ButtonGroupPanel() {
				this.init();
			}

			private void init() {
				this.createView();
				this.handleEvents();
			}

			private void createView() {
				this.setLayout(new FlowLayout(FlowLayout.LEADING));

				btnAdd = new JButton("Thêm");
				btnAdd.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/add.png")));
				this.add(btnAdd);

				btnEdit = new JButton("Sửa");
				btnEdit.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/edit.png")));
				btnEdit.setEnabled(false);
				this.add(btnEdit);

				btnRemove = new JButton("Xóa");
				btnRemove.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/remove.png")));
				btnRemove.setEnabled(false);
				this.add(btnRemove);

				btnSave = new JButton("Lưu");
				btnSave.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/save.png")));
				btnSave.setEnabled(false);
				this.add(btnSave);

				btnSkip = new JButton("Bỏ qua");
				btnSkip.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/skip.png")));
				btnSkip.setEnabled(false);
				this.add(btnSkip);
			}

			private void handleEvents() {
				btnAdd.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						tblGoods.clearSelection();
						clearForm();
						btnAdd.setEnabled(false);
						btnEdit.setEnabled(false);
						btnRemove.setEnabled(false);
						btnSave.setEnabled(true);
						btnSkip.setEnabled(true);
						action = ADD;
						setFormLock(false);
						cboStocks.requestFocus();
					}
				});
				btnEdit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rowSelectedIndex = tblGoods.getSelectedRow();
						btnAdd.setEnabled(false);
						btnEdit.setEnabled(false);
						btnRemove.setEnabled(false);
						btnSave.setEnabled(true);
						btnSkip.setEnabled(true);
						action = EDIT;
						setFormLock(false);
						cboStocks.requestFocus();
					}
				});

				btnRemove.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rowSelectedIndex = tblGoods.getSelectedRow();
						int choice = JOptionPane.showConfirmDialog(display, "Bạn có thực sự muốn xóa không?",
								"Xóa hàng hóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
						if (choice == JOptionPane.YES_OPTION) {
							if (rowSelectedIndex > -1) {
								Goods g = lstGoods.get(rowSelectedIndex);
								try {
									boolean deleted = Controller.deleteGoods(g);
									if (deleted) {
										lstGoods.remove(rowSelectedIndex);
										refreshTblGoods(lstGoods);
										if (lstGoods.isEmpty()) {
											if (chkStocks.isSelected()) {
												loadStock(true);
											} else {
												loadStock(false);
											}
											handleRowSelectedTblStocks();
										}
										if (rowSelectedIndex == lstGoods.size()) {
											rowSelectedIndex--;
											showGoodsDetail(rowSelectedIndex);
										} else {
											showGoodsDetail(rowSelectedIndex);
										}
									} else {
										JOptionPane.showMessageDialog(display, "Không thể xóa mặt hàng này!");
									}
								} catch (SQLException e1) {
									JOptionPane.showMessageDialog(display, e1.getMessage());
								}
							}
						}
						action = REMOVE;
						setFormLock(true);
					}
				});

				btnSave.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						switch (action) {
						case ADD:
							try {
								validateForm();
								Stock stock = stocks.get(cboStocks.getSelectedIndex());
								System.out.println("cboStocks.getSelectedIndex() " + cboStocks.getSelectedIndex());
								Category category = categories.get(cboCategories.getSelectedIndex());
								Manufacturer manufacturer = manufacturers.get(cboManufacturers.getSelectedIndex());
								String code = txtGoodsCode.getText();
								String name = txtGoodsName.getText();
								Date expiryDate = Utils.parseDateInput(txtExpiryDate.getText());
								int importedPrice = StringUtils.parseInt(txtImportedPrice.getText());
								int exportedPrice = StringUtils.parseInt(txtExportedPrice.getText());
								int inStock = Integer.parseInt(txtInStock.getText());
								Goods g = new Goods(code, name, expiryDate, importedPrice, exportedPrice, inStock,
										category, manufacturer, stock);
								int id = Controller.createGoods(g);
								if (id > 0) {
									// Insert vào csdl thành công

									// Set id
									g.setId(id);

									// Thêm vào list goods
									// lstGoods.add(g);

									// Làm mới lại tblGoods - không cần thiết
									// refreshTblGoods(lstGoods);

									// refreshTblStock(lstStocks);

									// Hiện thị hàng hóa vừa nhập

									// B1: làm mới lại kho hàng nếu checkbox được chọn
									System.out.println("B1: Làm mới lại bảng kho nếu checkbox được chọn");
									if (chkStocks.isSelected()) {
										System.out.println("Checkbox được chọn!");
										loadStock(true);
									}

									// B2: Chọn kho hàng để hiển thị hàng hóa vừa nhập
									System.out.println("B2: Chọn kho hàng để hiển thị hàng hóa vừa nhập");
									int stockRowIndex = -1;
									for (Stock s : lstStocks) {
										if (s.getId() == g.getStock().getId()) {
											stockRowIndex = lstStocks.indexOf(s);
											break;
										}
									}
									System.out.println("Index bảng kho: " + stockRowIndex);
									Utils.scrollToVisible(tblStocks, stockRowIndex, 0);

									// B3: Xử lý trên dòng được chọn trên bảng kho hàng để tải lại dữ liệu hàng hóa
									// vào bảng hàng hóa
									System.out.println(
											"B3: Xử lý trên dòng được chọn trên bảng kho hàng để tải lại dữ liệu hàng hóa vào bảng hàng hóa");
									handleRowSelectedTblStocks();

									// B4: Hiển thị hàng hóa vừa nhập
									System.out.println("B4: Hiển thị hàng hóa vừa nhập");
									for (Goods goods : lstGoods) {
										if (g.getId() == goods.getId()) {
											rowSelectedIndex = lstGoods.indexOf(goods);
											break;
										}
									}
									System.out.println("Index goods " + rowSelectedIndex);
									Utils.scrollToVisible(tblGoods, rowSelectedIndex, 0);
									showGoodsDetail(rowSelectedIndex);
								} else {
									throw new FormValidationException("Không thể thêm hàng hóa này!");
								}
							} catch (DateFormatException e2) {
								txtExpiryDate.requestFocus();
								JOptionPane.showMessageDialog(display, e2.getMessage());
								return;
							} catch (FormValidationException | DuplicateValueException e2) {
								JOptionPane.showMessageDialog(display, e2.getMessage());
								return;
							} catch (SQLException e2) {
								JOptionPane.showMessageDialog(display, "Lỗi cơ sở dữ liệu!");
								return;
							} catch (Exception e2) {
								e2.printStackTrace();
								return;
							}
							break;
						case EDIT:
							try {
								validateForm();
								int rowIndex = tblGoods.getSelectedRow();
								int id = lstGoods.get(rowIndex).getId();
								Stock stock = stocks.get(cboStocks.getSelectedIndex());
								Category category = categories.get(cboCategories.getSelectedIndex());
								Manufacturer manufacturer = manufacturers.get(cboManufacturers.getSelectedIndex());
								String code = txtGoodsCode.getText();
								String name = txtGoodsName.getText();
								Date expiryDate = Utils.parseDateInput(txtExpiryDate.getText());
								int importedPrice = StringUtils.parseInt(txtImportedPrice.getText());
								int exportedPrice = StringUtils.parseInt(txtExportedPrice.getText());
								int inStock = StringUtils.parseInt(txtInStock.getText());
								Goods g = new Goods(id, code, name, expiryDate, importedPrice, exportedPrice, inStock,
										category, manufacturer, stock);
								boolean updated = Controller.updateGoods(g);
								if (updated) {
									System.out.println("B1: Làm mới lại bảng kho nếu checkbox được chọn");
									if (chkStocks.isSelected()) {
										System.out.println("Checkbox được chọn!");
										loadStock(true);
									}
									System.out.println("B2: Chọn kho hàng để hiển thị hàng hóa vừa nhập");
									int stockRowIndex = -1;
									for (Stock s : lstStocks) {
										if (s.getId() == g.getStock().getId()) {
											stockRowIndex = lstStocks.indexOf(s);
											break;
										}
									}
									System.out.println("Index bảng kho: " + stockRowIndex);
									Utils.scrollToVisible(tblStocks, stockRowIndex, 0);
									System.out.println(
											"B3: Xử lý dòng được chọn trên bảng kho hàng để tải lại dữ liệu hàng hóa vào bảng hàng hóa");
									handleRowSelectedTblStocks();
									System.out.println("B4: Hiển thị hàng hóa vừa nhập");
									for (Goods goods : lstGoods) {
										if (g.getId() == goods.getId()) {
											rowSelectedIndex = lstGoods.indexOf(goods);
											break;
										}
									}
									System.out.println("Index goods " + rowSelectedIndex);
									Utils.scrollToVisible(tblGoods, rowSelectedIndex, 0);
									showGoodsDetail(rowSelectedIndex);
								} else {
									throw new FormValidationException("Không thể sửa hàng hóa này!");
								}
							} catch (DateFormatException e2) {
								txtExpiryDate.requestFocus();
								JOptionPane.showMessageDialog(display, e2.getMessage());
								return;
							} catch (FormValidationException | DuplicateValueException e2) {
								JOptionPane.showMessageDialog(display, e2.getMessage());
								return;
							} catch (SQLException e2) {
								JOptionPane.showMessageDialog(display, "Lỗi cơ sở dữ liệu!");
								return;
							} catch (Exception e2) {
								e2.printStackTrace();
								return;
							}
							break;
						}
						btnAdd.setEnabled(true);
						btnEdit.setEnabled(true);
						btnRemove.setEnabled(true);
						btnSave.setEnabled(false);
						btnSkip.setEnabled(false);
						action = SAVE;
						setFormLock(true);
					}
				});

				btnSkip.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						action = SKIP;
						btnAdd.setEnabled(true);
						btnEdit.setEnabled(false);
						btnRemove.setEnabled(false);
						btnSave.setEnabled(false);
						btnSkip.setEnabled(false);
						showGoodsDetail(rowSelectedIndex);
						setFormLock(true);
					}
				});
			}

		}

		private class FormInputPanel extends JPanel {

			private static final long serialVersionUID = 1L;

			public FormInputPanel() {
				this.init();
			}

			private void init() {
				this.createView();
				this.loadData();
				setFormLock(true);
				this.handleEvents();
			}

			private void createView() {
				this.setLayout(new GridBagLayout());
				Insets insets = new Insets(5, 5, 5, 5);

				lblStocks = new JLabel("Kho");
				this.add(lblStocks, new GridBagConstraints(0, 0, 1, 1, 0.1, 0.0, 17, 0, insets, 0, 7));
				cboStocks = new JComboBox<>();
				this.add(cboStocks, new GridBagConstraints(1, 0, 7, 1, 0.9, 0.0, 17, 2, insets, 0, 7));

				lblGoodsCode = new JLabel("Mã sản phẩm");
				this.add(lblGoodsCode, new GridBagConstraints(0, 1, 1, 1, 0.1, 0.0, 17, 0, insets, 0, 7));
				txtGoodsCode = new JTextField();
				// txtGoodsCode.setHorizontalAlignment(JTextField.CENTER);
				this.add(txtGoodsCode, new GridBagConstraints(1, 1, 1, 1, 0.9, 0.0, 17, 2, insets, 0, 7));

				lblGoodsName = new JLabel("Tên sản phẩm");
				this.add(lblGoodsName, new GridBagConstraints(2, 1, 1, 1, 0.1, 0.0, 13, 0, insets, 0, 7));
				txtGoodsName = new JTextField();
				this.add(txtGoodsName, new GridBagConstraints(3, 1, 5, 1, 0.9, 0.0, 17, 2, insets, 0, 7));

				lblCategories = new JLabel("Phân loại");
				this.add(lblCategories, new GridBagConstraints(0, 2, 1, 1, 0.1, 0.0, 17, 0, insets, 0, 7));
				cboCategories = new JComboBox<>();
				this.add(cboCategories, new GridBagConstraints(1, 2, 7, 1, 0.9, 0.0, 17, 2, insets, 0, 7));

				lblManufacturers = new JLabel("Hãng sản xuất");
				this.add(lblManufacturers, new GridBagConstraints(0, 3, 1, 1, 0.1, 0.0, 17, 0, insets, 0, 7));
				cboManufacturers = new JComboBox<>();
				this.add(cboManufacturers, new GridBagConstraints(1, 3, 7, 1, 0.9, 0.0, 17, 2, insets, 0, 7));

				lblExpiryDate = new JLabel("Hạn sử dụng");
				this.add(lblExpiryDate, new GridBagConstraints(0, 4, 1, 1, 0.1, 0.0, 17, 0, insets, 0, 7));
				MaskFormatter format = null;
				try {
					format = new MaskFormatter("##/##/####");
					format.setPlaceholderCharacter('_');
					format.setValidCharacters("0123456789");
				} catch (ParseException e) {
					e.printStackTrace();
				}
				txtExpiryDate = new JFormattedTextField(format);
				// txtExpiryDate.setHorizontalAlignment(JTextField.CENTER);
				this.add(txtExpiryDate, new GridBagConstraints(1, 4, 1, 1, 0.9, 0.0, 17, 2, insets, 0, 7));

				lblImportedPrice = new JLabel("Giá nhập");
				this.add(lblImportedPrice, new GridBagConstraints(2, 4, 1, 1, 0.1, 0.0, 13, 0, insets, 0, 7));

				NumberFormat importedPriceFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
				importedPriceFormat.setMaximumFractionDigits(0);
				txtImportedPrice = new JFormattedTextField(importedPriceFormat);
				// txtImportedPrice.setHorizontalAlignment(JTextField.RIGHT);
				this.add(txtImportedPrice, new GridBagConstraints(3, 4, 1, 1, 0.9, 0.0, 17, 2, insets, 0, 7));

				lblExportedPrice = new JLabel("Giá bán");
				this.add(lblExportedPrice, new GridBagConstraints(4, 4, 1, 1, 0.1, 0.0, 13, 0, insets, 0, 7));
				NumberFormat exportedPriceFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
				exportedPriceFormat.setMaximumFractionDigits(0);
				txtExportedPrice = new JFormattedTextField(exportedPriceFormat);
				// txtExportedPrice.setHorizontalAlignment(JTextField.RIGHT);
				this.add(txtExportedPrice, new GridBagConstraints(5, 4, 1, 1, 0.9, 0.0, 17, 2, insets, 0, 7));

				lblInStock = new JLabel("Số lượng tồn kho");
				this.add(lblInStock, new GridBagConstraints(0, 5, 1, 1, 0.1, 0.0, 17, 0, insets, 0, 7));
				NumberFormat inStockFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
				exportedPriceFormat.setMaximumFractionDigits(0);
				txtInStock = new JFormattedTextField(inStockFormat);
				// txtInStock.setHorizontalAlignment(JTextField.RIGHT);
				this.add(txtInStock, new GridBagConstraints(1, 5, 1, 1, 0.9, 0.0, 17, 2, insets, 0, 7));
			}

			private void handleEvents() {
				cboStocks.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER)
							txtGoodsCode.requestFocus();
					}
				});
				txtGoodsCode.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() != KeyEvent.VK_LEFT && e.getKeyCode() != KeyEvent.VK_RIGHT) {
							String text = txtGoodsCode.getText().replaceAll("[^a-zA-Z0-9]", "");
							txtGoodsCode.setText(text.toUpperCase());
						}
						if (e.getKeyCode() == KeyEvent.VK_ENTER)
							txtGoodsName.requestFocus();
					}
				});

				txtGoodsName.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER)
							cboCategories.requestFocus();
					}
				});

				txtGoodsName.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtGoodsName.setText(txtGoodsName.getText().replaceAll("\\s+", " ").trim());
					}
				});

				cboCategories.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							cboManufacturers.requestFocus();
						}
					}
				});

				cboManufacturers.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							txtExpiryDate.requestFocus();
						}
					}
				});

				txtExpiryDate.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							txtImportedPrice.requestFocus();
						} else if (e.getKeyCode() == KeyEvent.VK_UP) {
							String date = txtExpiryDate.getText();
							try {
								Calendar today = Calendar.getInstance();
								SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
								long curDate = format.parse(date).getTime();
								today.setTime(new Date(curDate));
								today.add(Calendar.DAY_OF_YEAR, 1);
								String nextDate = format.format(today.getTime());
								System.out.println(nextDate);
								txtExpiryDate.setText(String.valueOf(nextDate));
							} catch (ParseException e1) {
								e1.printStackTrace();
							}
						} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
							String date = txtExpiryDate.getText();
							try {
								Calendar today = Calendar.getInstance();
								SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
								long curDate = format.parse(date).getTime();
								today.setTime(new Date(curDate));
								today.add(Calendar.DAY_OF_YEAR, -1);
								String prevDate = format.format(today.getTime());
								System.out.println(prevDate);
								txtExpiryDate.setText(String.valueOf(prevDate));
							} catch (ParseException e1) {
								e1.printStackTrace();
							}
						}
					}
				});

				txtImportedPrice.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							txtExportedPrice.requestFocus();
						}
						if (e.getKeyCode() == KeyEvent.VK_UP) {
							try {
								String imported = txtImportedPrice.getText();
								int importedPrice = 0;
								if (imported.isEmpty()) {
									txtImportedPrice.setText(String.valueOf(importedPrice));
								} else {
									importedPrice = StringUtils.parseInt(imported);
									if (importedPrice < 0) {
										JOptionPane.showMessageDialog(display, "Không được nhập số âm, mời nhập lại!");
										txtImportedPrice.requestFocus();
										return;
									}
									importedPrice++;
									txtImportedPrice.setText(String.valueOf(importedPrice));
								}

							} catch (Exception e2) {
								JOptionPane.showMessageDialog(display, "Bạn chưa nhập đúng định dạng, mời nhập lại!");
								txtImportedPrice.requestFocus();
								return;
							}

						}
						if (e.getKeyCode() == KeyEvent.VK_DOWN) {
							try {
								String imported = txtImportedPrice.getText();
								int importedPrice = 0;
								if (imported.isEmpty()) {
									txtImportedPrice.setText(importedPrice + "");
								} else {
									importedPrice = StringUtils.parseInt(imported);
									if (importedPrice <= 0) {
										JOptionPane.showMessageDialog(display, "Không được nhập số âm, mời nhập lại!");
										txtImportedPrice.requestFocus();
										return;
									}
									importedPrice--;
									txtImportedPrice.setText(String.valueOf(importedPrice));
								}

							} catch (Exception e2) {
								JOptionPane.showMessageDialog(display, "Bạn chưa nhập đúng định dạng, mời nhập lại!");
								txtImportedPrice.requestFocus();
								return;
							}
						}
					}
				});

				txtExportedPrice.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							txtInStock.requestFocus();
						}
						if (e.getKeyCode() == KeyEvent.VK_UP) {
							try {
								String exported = txtExportedPrice.getText();
								int exportedPrice = 0;
								if (exported.isEmpty()) {
									txtExportedPrice.setText(String.valueOf(exportedPrice));
								} else {
									exportedPrice = StringUtils.parseInt(exported);
									if (exportedPrice < 0) {
										JOptionPane.showMessageDialog(display, "Không được nhập số âm, mời nhập lại!");
										txtExportedPrice.requestFocus();
										return;
									}
									exportedPrice++;
									txtExportedPrice.setText(String.valueOf(exportedPrice));
								}

							} catch (Exception e2) {
								JOptionPane.showMessageDialog(display, "Bạn chưa nhập đúng định dạng, mời nhập lại!");
								txtExportedPrice.requestFocus();
								return;
							}

						}
						if (e.getKeyCode() == KeyEvent.VK_DOWN) {
							try {
								String exported = txtExportedPrice.getText();
								int exportedPrice = 0;
								if (exported.isEmpty()) {
									txtExportedPrice.setText(String.valueOf(exportedPrice));
								} else {
									exportedPrice = StringUtils.parseInt(exported);
									if (exportedPrice <= 0) {
										JOptionPane.showMessageDialog(display, "Không được nhập số âm, mời nhập lại!");
										txtExportedPrice.requestFocus();
										return;
									}
									exportedPrice--;
									txtExportedPrice.setText(String.valueOf(exportedPrice));
								}

							} catch (Exception e2) {
								JOptionPane.showMessageDialog(display, "Bạn chưa nhập đúng định dạng, mời nhập lại!");
								txtExportedPrice.requestFocus();
								return;
							}
						}
					}
				});

				txtInStock.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_UP) {
							try {
								String inStock = txtInStock.getText();
								int inStockValue = 0;
								if (inStock.isEmpty()) {
									txtInStock.setText(String.valueOf(inStockValue));
								} else {
									inStockValue = StringUtils.parseInt(inStock);
									if(inStockValue < 0) {
										JOptionPane.showMessageDialog(display, "Không được nhập số âm, mời nhập lại!");
										txtInStock.requestFocus();
										return;
									}
									inStockValue++;
									txtInStock.setText(inStockValue + "");
								}

							} catch (Exception e2) {
								JOptionPane.showMessageDialog(display, "Bạn chưa nhập đúng định dạng, mời nhập lại!");
								txtInStock.requestFocus();
								return;
							}

						}
						if (e.getKeyCode() == KeyEvent.VK_DOWN) {
							try {
								String inStock = txtInStock.getText();
								int inStockValue = 0;
								if (inStock.isEmpty()) {
									txtInStock.setText(inStockValue + "");
								} else {
									inStockValue = StringUtils.parseInt(inStock);
									if (inStockValue > 0) {
										inStockValue--;
									}
									if (inStockValue < 0) {
										JOptionPane.showMessageDialog(display, "Không được nhập số âm, mời nhập lại!");
										txtExportedPrice.requestFocus();
										return;
									}
									txtInStock.setText(inStockValue + "");
								}

							} catch (Exception e2) {
								JOptionPane.showMessageDialog(display, "Bạn chưa nhập đúng định dạng, mời nhập lại!");
								txtExportedPrice.requestFocus();
								return;
							}
						}
					}
				});
			}

			private void loadData() {
				try {
					stocks = Controller.loadStocks(false);
					stocks.forEach(o -> {
						cboStocks.addItem(o.getName());
					});
					cboStocks.setSelectedIndex(-1);

					categories = Controller.loadCategories();
					categories.forEach(o -> {
						cboCategories.addItem(o.getName());
					});
					cboCategories.setSelectedIndex(-1);

					manufacturers = Controller.loadManufacturers();
					manufacturers.forEach(o -> {
						cboManufacturers.addItem(o.getName());
					});
					cboManufacturers.setSelectedIndex(-1);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}

		private class FormSeachGroupPanel extends JPanel {
			private static final long serialVersionUID = 1L;

			private List<Integer> rowIndexes = new ArrayList<>();
			private int position = -1;
			private int rowIndex = -1;

			public FormSeachGroupPanel() {
				this.init();
			}

			private void init() {
				this.createView();
				this.handleEvents();
			}

			private void createView() {
				this.setLayout(new BorderLayout());
				// LEFT
				JPanel pnlLeft = new JPanel(new GridBagLayout());
				lblSearch = new JLabel("Tìm kiếm");
				Insets insets = new Insets(5, 5, 5, 5);
				pnlLeft.add(lblSearch, new GridBagConstraints(0, 0, 1, 1, 0.1, 0.0, 17, 0, insets, 0, 7));
				txtSearch = new JTextField(14);
				pnlLeft.add(txtSearch, new GridBagConstraints(1, 0, 2, 1, 0.1, 0.0, 17, 2, insets, 0, 7));
				btnSearch = new JButton("Tìm");
				btnSearch.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/search.png")));
				pnlLeft.add(btnSearch, new GridBagConstraints(3, 0, 1, 1, 0.1, 0.0, 17, 0, insets, 0, 0));

				lblOrderBy = new JLabel("Sắp xếp theo");
				pnlLeft.add(lblOrderBy, new GridBagConstraints(0, 1, 1, 1, 0.1, 0.0, 17, 0, insets, 0, 0));

				ButtonGroup buttonGroup = new ButtonGroup();
				rdoManufacturer = new JRadioButton("Hãng sản xuất");
				rdoManufacturer.setSelected(true);
				pnlLeft.add(rdoManufacturer, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 17, 0, insets, 0, 0));

				rdoCategory = new JRadioButton("Phân loại");
				buttonGroup.add(rdoCategory);
				buttonGroup.add(rdoManufacturer);
				pnlLeft.add(rdoCategory, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, 17, 0, insets, 0, 0));

				this.add(pnlLeft, BorderLayout.WEST);

				// RIGHT
				JPanel pnlRight = new JPanel(new GridBagLayout());
				btnExport = new JButton("Kết xuất");
				btnExport.setIcon(new ImageIcon(getClass().getResource("/app/stockmanagement/icons/excel.png")));
				pnlRight.add(new JPanel(), new GridBagConstraints(0, 0, 1, 1, 0.5, 0.5, 13, 0, insets, 0, 0));
				pnlRight.add(btnExport, new GridBagConstraints(0, 1, 1, 1, 0.5, 0.5, 13, 0, insets, 0, 0));
				this.add(pnlRight, BorderLayout.CENTER);
			}

			private void handleEvents() {
				txtSearch.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							if (rowIndexes.size() > 0) {
								if (position > rowIndexes.size() - 1) {
									position = 0;
								}
								int rowIndex = rowIndexes.get(position);
								Utils.scrollToVisible(tblGoods, rowIndex, 0);
								showGoodsDetail(rowIndex);
								position++;
							}
						} else {
							String text = txtSearch.getText();
							rowIndexes.clear();
							if (text.length() > 0) {
								boolean found = false;
								for (Goods g : lstGoods) {
									if (g.getCode().toLowerCase().contains(text.toLowerCase())
											|| g.getName().toLowerCase().contains(text.toLowerCase())) {
										rowIndexes.add(lstGoods.indexOf(g));
										found = true;
									}
								}
								if (!found) {
									clearForm();
								} else {
									position = 0;
									int rowIndex = rowIndexes.get(position);
									Utils.scrollToVisible(tblGoods, rowIndex, 0);
									showGoodsDetail(rowIndex);
									position++;
								}
							} else {
								clearForm();
							}
						}
					}
				});

				txtSearch.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseReleased(MouseEvent e) {
						if (action == ADD || action == EDIT) {
							int c = JOptionPane.showConfirmDialog(display,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								btnAdd.setEnabled(true);
								btnEdit.setEnabled(true);
								btnRemove.setEnabled(true);
								btnSave.setEnabled(false);
								btnSkip.setEnabled(false);
								action = SKIP;
								setFormLock(true);
							} else {

							}
						}
					}
				});

				btnSearch.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (action == ADD || action == EDIT) {
							int c = JOptionPane.showConfirmDialog(display,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								btnAdd.setEnabled(true);
								btnEdit.setEnabled(true);
								btnRemove.setEnabled(true);
								btnSave.setEnabled(false);
								btnSkip.setEnabled(false);
								action = SKIP;
								setFormLock(true);
								if (rowIndexes.size() > 0) {
									if (position > rowIndexes.size() - 1) {
										position = 0;
									}
									rowIndex = rowIndexes.get(position);
									Utils.scrollToVisible(tblGoods, rowIndex, 0);
									showGoodsDetail(rowIndex);
									position++;
								}
							}
						} else {
							if (rowIndexes.size() > 0) {
								if (position > rowIndexes.size() - 1) {
									position = 0;
								}
								rowIndex = rowIndexes.get(position);
								Utils.scrollToVisible(tblGoods, rowIndex, 0);
								showGoodsDetail(rowIndex);
								position++;
							}
						}
					}
				});

				rdoCategory.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (action == ADD || action == EDIT) {
							int c = JOptionPane.showConfirmDialog(display,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								btnAdd.setEnabled(true);
								btnEdit.setEnabled(true);
								btnRemove.setEnabled(true);
								btnSave.setEnabled(false);
								btnSkip.setEnabled(false);
								action = SKIP;
								setFormLock(true);
								rowIndex = tblGoods.getSelectedRow();
								int goodsId = lstGoods.get(rowIndex).getId();
								sortBy(OrderBy.CATEGORY);
								refreshTblGoods(lstGoods);
								for (Goods g : lstGoods) {
									if (g.getId() == goodsId) {
										rowIndex = lstGoods.indexOf(g);
										showGoodsDetail(rowIndex);
										break;
									}
								}
								orderBy = OrderBy.CATEGORY;
							} else {
								setOrderBy(orderBy);
							}
						} else {
							rowIndex = tblGoods.getSelectedRow();
							int goodsId = lstGoods.get(rowIndex).getId();
							sortBy(OrderBy.CATEGORY);
							refreshTblGoods(lstGoods);
							for (Goods g : lstGoods) {
								if (g.getId() == goodsId) {
									rowIndex = lstGoods.indexOf(g);
									showGoodsDetail(rowIndex);
									break;
								}
							}
							orderBy = OrderBy.CATEGORY;
						}

					}
				});

				rdoManufacturer.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (action == ADD || action == EDIT) {
							int c = JOptionPane.showConfirmDialog(display,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								btnAdd.setEnabled(true);
								btnEdit.setEnabled(true);
								btnRemove.setEnabled(true);
								btnSave.setEnabled(false);
								btnSkip.setEnabled(false);
								action = SKIP;
								setFormLock(true);
								rowIndex = tblGoods.getSelectedRow();
								int goodsId = lstGoods.get(rowIndex).getId();
								sortBy(OrderBy.MANUFACTURER);
								refreshTblGoods(lstGoods);
								for (Goods g : lstGoods) {
									if (g.getId() == goodsId) {
										rowIndex = lstGoods.indexOf(g);
										showGoodsDetail(rowIndex);
										break;
									}
								}
								orderBy = OrderBy.MANUFACTURER;
							} else {
								setOrderBy(orderBy);
							}
						} else {
							rowIndex = tblGoods.getSelectedRow();
							int goodsId = lstGoods.get(rowIndex).getId();
							sortBy(OrderBy.MANUFACTURER);
							refreshTblGoods(lstGoods);
							for (Goods g : lstGoods) {
								if (g.getId() == goodsId) {
									rowIndex = lstGoods.indexOf(g);
									showGoodsDetail(rowIndex);
									break;
								}
							}
							orderBy = OrderBy.MANUFACTURER;
						}

					}
				});

				btnExport.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (action == ADD || action == EDIT) {
							int c = JOptionPane.showConfirmDialog(display,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								btnAdd.setEnabled(true);
								btnEdit.setEnabled(true);
								btnRemove.setEnabled(true);
								btnSave.setEnabled(false);
								btnSkip.setEnabled(false);
								action = SKIP;
								setFormLock(true);
								showGoodsDetail(rowIndex);
								export();
							}
						} else {
							export();
						}
					}
				});
			}

			private void export() {
				int rowCount = tblGoods.getRowCount();
				if (rowCount == 0) {
					return;
				}
				int select = tblStocks.getSelectedRow();
				String fileName = tblStocks.getValueAt(select, 1) + "";
				File file = new File(fileName);

				XSSFWorkbook workbook = new XSSFWorkbook();

				XSSFSheet sheet = workbook.createSheet("Danh sách hàng hóa");
				int rownum = 0;

				// Header
				Row firstRow = sheet.createRow(rownum++);
				for (int i = 0; i < tblGoods.getColumnCount(); i++) {
					firstRow.createCell(i).setCellValue(tblGoods.getColumnName(i));
				}

				// Content
				for (int row = 0; row < rowCount; row++) {
					Row rowContent = sheet.createRow(rownum++);
					for (int column = 0; column < tblGoods.getColumnCount(); column++) {
						rowContent.createCell(column).setCellValue(String.valueOf(tblGoods.getValueAt(row, column)));
						sheet.autoSizeColumn(column);
					}
				}
				try {
					String extension = ".xlsx";
					workbook.write(new FileOutputStream(file + extension));
					workbook.close();
					Desktop.getDesktop().open(new File(file.getAbsolutePath() + extension));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		private class GoodsPanel extends JPanel {
			private static final long serialVersionUID = 1L;
			private int rowIndex = -1;

			public GoodsPanel() {
				this.init();
			}

			private void init() {
				this.createView();
				this.handleEvents();
				handleRowSelectedTblStocks();
			}

			private void createView() {
				this.setLayout(new BorderLayout());
				this.setBorder(new EmptyBorder(0, 3, 0, 3));

				String[] columnNames = { "STT", "Mã hàng hóa", "Tên hàng hóa", "Phân loại", "Hãng sản xuất",
						"Số lượng tồn kho" };
				goodsTableModel = new DefaultTableModel(columnNames, 0) {
					private static final long serialVersionUID = 1L;

					@Override
					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};
				tblGoods = new JTable();
				tblGoods.setModel(goodsTableModel);
				DefaultTableCellRenderer cellRenderer1 = new DefaultTableCellRenderer();
				cellRenderer1.setHorizontalAlignment(JLabel.CENTER);
				tblGoods.getColumnModel().getColumn(0).setMinWidth(20);
				tblGoods.getColumnModel().getColumn(0).setPreferredWidth(50);
				tblGoods.getColumnModel().getColumn(0).setMaxWidth(50);
				tblGoods.getColumnModel().getColumn(0).setCellRenderer(cellRenderer1);
				tblGoods.getColumnModel().getColumn(1).setMinWidth(100);
				tblGoods.getColumnModel().getColumn(1).setPreferredWidth(100);
				tblGoods.getColumnModel().getColumn(1).setCellRenderer(cellRenderer1);
				tblGoods.getColumnModel().getColumn(2).setMinWidth(100);
				tblGoods.getColumnModel().getColumn(2).setPreferredWidth(300);
				tblGoods.getColumnModel().getColumn(3).setMinWidth(50);
				tblGoods.getColumnModel().getColumn(3).setCellRenderer(cellRenderer1);
				tblGoods.getColumnModel().getColumn(4).setCellRenderer(cellRenderer1);
				DefaultTableCellRenderer cellRenderer2 = new DefaultTableCellRenderer();
				cellRenderer2.setHorizontalAlignment(JLabel.RIGHT);
				tblGoods.getColumnModel().getColumn(5).setCellRenderer(cellRenderer2);
				tblGoods.getTableHeader().setReorderingAllowed(false);
				tblGoods.setRowHeight(28);

				tblGoods.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

				scrllGoodsTable = new JScrollPane();
				scrllGoodsTable.setViewportView(tblGoods);

				this.add(scrllGoodsTable);
			}

			private void handleEvents() {
				tblGoods.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) {
						if (action == ADD || action == EDIT) {
							int c = JOptionPane.showConfirmDialog(display,
									"Mọi chỉnh sửa bạn vừa thực hiện sẽ bị hủy. Bạn có muốn hủy không?", "Xác nhận",
									JOptionPane.YES_NO_OPTION);
							if (c == JOptionPane.YES_OPTION) {
								btnAdd.setEnabled(true);
								btnEdit.setEnabled(true);
								btnRemove.setEnabled(true);
								btnSave.setEnabled(false);
								btnSkip.setEnabled(false);
								action = SKIP;
								setFormLock(true);
								rowIndex = tblGoods.getSelectedRow();
								showGoodsDetail(rowIndex);
							} else {
								System.out.println(rowIndex);
								Utils.scrollToVisible(tblGoods, rowIndex, 0);
							}
						} else {
							rowIndex = tblGoods.getSelectedRow();
							showGoodsDetail(rowIndex);
						}
					}
				});

				tblGoods.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN
								|| e.getKeyCode() == KeyEvent.VK_ENTER) {
							rowIndex = tblGoods.getSelectedRow();
							showGoodsDetail(rowIndex);
						}
					}
				});
			}
		}

	}

	private void setFormLock(boolean lock) {
		txtGoodsCode.setEditable(!lock);
		txtGoodsName.setEditable(!lock);
		txtExpiryDate.setEditable(!lock);
		txtImportedPrice.setEditable(!lock);
		txtExportedPrice.setEditable(!lock);
		txtInStock.setEditable(!lock);
		cboCategories.setEnabled(!lock);
		cboManufacturers.setEnabled(!lock);
		cboStocks.setEnabled(!lock);
	}

	private void handleRowSelectedTblStocks() {
		int rowIndex = tblStocks.getSelectedRow();
		if (rowIndex > -1) {
			Stock s = lstStocks.get(rowIndex);
			try {
				lstGoods = Controller.loadGoodsInSock(s, getOrderBy());
				sortBy(getOrderBy());
				refreshTblGoods(lstGoods);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} else {
			clearForm();
			goodsTableModel.setRowCount(0);
			lstGoods.clear();
		}
	}

	private void showGoodsDetail(int rowIndex) {
		if (rowIndex > -1) {
			Utils.scrollToVisible(tblGoods, rowIndex, 0);
			Goods g = lstGoods.get(rowIndex);
			cboStocks.setSelectedItem(g.getStock().getName());
			txtGoodsCode.setText(g.getCode());
			txtGoodsName.setText(g.getName());
			cboCategories.setSelectedItem(g.getCategory().getName());
			cboManufacturers.setSelectedItem(g.getManufacturer().getName());
			txtExpiryDate.setText(StringUtils.formatDate(g.getExpiryDate()));
			txtImportedPrice.setText(StringUtils.format(g.getImportedPrice()));
			txtExportedPrice.setText(StringUtils.format(g.getExportedPrice()));
			txtInStock.setText(StringUtils.format(g.getInStock()));
			btnEdit.setEnabled(true);
			btnRemove.setEnabled(true);
			System.out.println("action " + action);
			if (action == ADD || action == EDIT) {
				btnAdd.setEnabled(false);
				btnEdit.setEnabled(false);
				btnRemove.setEnabled(false);
				btnSave.setEnabled(true);
				btnSkip.setEnabled(true);
			}
		} else {
			clearForm();
		}
	}

	private void clearForm() {
		cboStocks.setSelectedIndex(-1);
		txtGoodsCode.setText(null);
		txtGoodsName.setText(null);
		cboCategories.setSelectedIndex(-1);
		cboManufacturers.setSelectedIndex(-1);
		txtExpiryDate.setText(null);
		txtImportedPrice.setText(null);
		txtExportedPrice.setText(null);
		txtInStock.setText(null);
		tblGoods.clearSelection();

		btnAdd.setEnabled(true);
		btnEdit.setEnabled(false);
		btnRemove.setEnabled(false);
		btnSave.setEnabled(false);
		btnSkip.setEnabled(false);
		action = SKIP;
		setFormLock(true);
	}

	private OrderBy getOrderBy() {
		if (rdoManufacturer.isSelected()) {
			return OrderBy.MANUFACTURER;
		} else {
			return OrderBy.CATEGORY;
		}
	}

	private void setOrderBy(OrderBy orderBy) {
		if (orderBy == OrderBy.CATEGORY) {
			rdoCategory.setSelected(true);
		} else {
			rdoManufacturer.setSelected(true);
		}
	}

	private void sortBy(OrderBy by) {
		switch (by) {
		case CATEGORY:
			Collections.sort(lstGoods, new Comparator<Goods>() {
				@Override
				public int compare(Goods o1, Goods o2) {
					return o1.getCategory().getName().compareToIgnoreCase(o2.getCategory().getName());
				}
			});
			break;
		default:
			Collections.sort(lstGoods, new Comparator<Goods>() {
				@Override
				public int compare(Goods o1, Goods o2) {
					return o1.getManufacturer().getName().compareToIgnoreCase(o2.getManufacturer().getName());
				}
			});
			break;
		}
	}

	private void handleEvent(JFrame frame) {
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int choice = JOptionPane.showConfirmDialog(frame, "Bạn có muốn thoát chương trình không?", "Thoát",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (choice == JOptionPane.YES_OPTION) {
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					Controller.exit(true);
				}
			}
		});
	}

	private void refreshTblStock(List<Stock> lstStocks) {
		tblStockModel.setRowCount(0);
		lstStocks.forEach(s -> {
			tblStockModel.addRow(new Object[] { lstStocks.indexOf(s) + 1, s.getName() });
		});

		if (lstStocks.size() > 0) {
			Utils.scrollToVisible(tblStocks, 0, 0);
		}

	}

	private void refreshTblGoods(List<Goods> lstGoods) {
		goodsTableModel.setRowCount(0);
		lstGoods.forEach(o -> {
			goodsTableModel.addRow(new Object[] { lstGoods.indexOf(o) + 1, o.getCode(), o.getName(),
					o.getCategory().getName(), o.getManufacturer().getName(), o.getInStock() });
		});
		if (lstGoods.size() > 0) {
			showGoodsDetail(0);
		} else {
			clearForm();
		}
	}

	private void validateForm() throws FormValidationException {
		if (cboStocks.getSelectedIndex() == -1) {
			cboStocks.requestFocus();
			throw new FormValidationException("Bạn chưa chọn kho hàng!");
		}
		if (txtGoodsCode.getText().trim().isEmpty()) {
			txtGoodsCode.requestFocus();
			throw new FormValidationException("Bạn chưa nhập mã hàng hóa!");
		}
		if (txtGoodsName.getText().trim().isEmpty()) {
			txtGoodsName.requestFocus();
			throw new FormValidationException("Bạn chưa nhập tên hàng hóa!");
		}
		if (cboCategories.getSelectedIndex() == -1) {
			cboCategories.requestFocus();
			throw new FormValidationException("Bạn chưa chọn loại mặt hàng!");
		}
		if (cboManufacturers.getSelectedIndex() == -1) {
			cboManufacturers.requestFocus();
			throw new FormValidationException("Bạn chưa chọn hãng sản xuất!");
		}
		if (txtExpiryDate.getText().trim().isEmpty()) {
			txtExpiryDate.requestFocus();
			throw new FormValidationException("Bạn chưa nhập hạn sử dụng!");
		}
		String importedPrice = txtImportedPrice.getText().trim();
		if (importedPrice.isEmpty()) {
			txtImportedPrice.requestFocus();
			throw new FormValidationException("Bạn chưa nhập giá nhập!");
		}
		if (!StringUtils.isInteger(importedPrice)) {
			txtImportedPrice.requestFocus();
			throw new FormValidationException("Giá nhập yêu cầu nhập vào ký tự số!");
		} else {
			try {
				if (StringUtils.parseInt(importedPrice) < 0) {
					txtImportedPrice.requestFocus();
					throw new FormValidationException("Không thể nhập giá nhập nhỏ hơn 0!");
				}
			} catch (FormValidationException e) {
				txtInStock.requestFocus();
				throw e;
			} catch (Exception e) {
				throw new FormValidationException("Lỗi định dạng!");
			}
		}
		String exportedPrice = txtExportedPrice.getText().trim();
		if (exportedPrice.isEmpty()) {
			txtExportedPrice.requestFocus();
			throw new FormValidationException("Bạn chưa nhập giá bán!");
		}
		if (!StringUtils.isInteger(exportedPrice)) {
			txtExportedPrice.requestFocus();
			throw new FormValidationException("Giá bán yêu cầu nhập vào ký tự số!");
		} else {
			try {
				if (StringUtils.parseInt(exportedPrice) < 0) {
					txtExportedPrice.requestFocus();
					throw new FormValidationException("Không thể nhập giá bán nhỏ hơn 0!");
				}
			} catch (FormValidationException e) {
				txtInStock.requestFocus();
				throw e;
			} catch (Exception e) {
				throw new FormValidationException("Lỗi định dạng!");
			}
		}
		String inStock = txtInStock.getText().trim();
		if (inStock.isEmpty()) {
			txtInStock.requestFocus();
			throw new FormValidationException("Bạn chưa nhập số lượng hàng tồn kho!");
		}
		if (!StringUtils.isInteger(inStock)) {
			txtInStock.requestFocus();
			throw new FormValidationException("Số lượng hàng tồn kho phải là số nguyên!");
		} else {
			try {
				if (StringUtils.parseInt(inStock) < 0) {
					txtInStock.requestFocus();
					throw new FormValidationException("Số lượng hàng tồn kho phải là lớn hơn hoặc bằng 0!");
				}
			} catch (FormValidationException e) {
				txtInStock.requestFocus();
				throw e;
			} catch (Exception e) {
				txtInStock.requestFocus();
				throw new FormValidationException("Lỗi định dạng!");
			}
		}
	}

	private void loadStock(boolean havingGoods) {
		try {
			lstStocks = Controller.loadStocks(havingGoods);
			refreshTblStock(lstStocks);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
