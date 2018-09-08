
import java.awt.*;

import javax.swing.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class Canvas extends JPanel // subclass of JPanel
{

	public ArrayList<DShape> listShapes = new ArrayList<DShape>();
	public static DShape[] knobes = new DRect[4];
	public static DShape movingKnob, anchorKnob;
	public static Point movingPoint, anchorPoint;
	boolean draggingKnob = false;
	static int listSize;
	public DShape selected;
	private Whiteboard whiteboard;
	private int lastX, lastY;
	final public static int KNOB_SIZE = 9;

	public Canvas(Whiteboard whiteBoard) {
		this.whiteboard = whiteBoard;
		listSize = 0;

		this.setBackground(Color.WHITE);

		for (int x = 0; x < 4; x++) {
			DRectModel shapeModel = new DRectModel();
			Whiteboard.setCoordinates(shapeModel, new Rectangle(-1000, -1000, KNOB_SIZE, KNOB_SIZE));
			knobes[x] = new DRect(shapeModel, this);
			knobes[x].model.setColor(Color.BLACK);
		}

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (Whiteboard.mode != Whiteboard.CLIENT_MODE) {
					mouseClickedTO(e);
					lastX = e.getX();
					lastY = e.getY();
				}
			}
		});

		addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				draggingKnob = false;
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				if (Whiteboard.mode != Whiteboard.CLIENT_MODE) {
					int dx = e.getX() - lastX;
					int dy = e.getY() - lastY;
					lastX = e.getX();
					lastY = e.getY();

					if (draggingKnob) {
						if (selected instanceof DLine) {

							movingPoint.x += dx;
							movingPoint.y += dy;

							int knobNumber = -1;
							for (int i = 0; i < 4; i++) {
								if (knobes[i] == movingKnob)
									knobNumber = i;
							}

							if (knobNumber == 0 || knobNumber == 1) {
								((DLineModel) selected.model).setP1(movingPoint);
							}
							if (knobNumber == 2 || knobNumber == 3) {
								((DLineModel) selected.model).setP2(movingPoint);
							}
							updateKnobs();
							selected.modelChanged(selected.model);
						} else {
							movingPoint.x += dx;
							movingPoint.y += dy;
							Rectangle newRec = new Rectangle(movingPoint);
							newRec.add(anchorPoint);
							Whiteboard.setCoordinates(selected.model, newRec);

							updateKnobs();
							selected.modelChanged(selected.model);
						}
					} else if (hasSelected()) {
						if (selected instanceof DLine) {
							Point p = new Point(((DLineModel) selected.model).getP1().x + dx,
									((DLineModel) selected.model).getP1().y + dy);
							((DLineModel) selected.model).setP1(p);

							p = new Point(((DLineModel) selected.model).getP2().x + dx,
									((DLineModel) selected.model).getP2().y + dy);
							((DLineModel) selected.model).setP2(p);

							updateKnobs();
							selected.modelChanged(selected.model);
						} else {
							selected.model.setX(selected.model.getX() + dx);
							selected.model.setY(selected.model.getY() + dy);

							updateKnobs();
							selected.modelChanged(selected.model);
						}
					}
				}
			}
		});
	}

	public boolean hasSelected() {
		return selected != null;
	}

	public void changeSelection(DShape shape) {
		selected = shape;
		updateKnobs();

		if (selected instanceof DText) {
			whiteboard.updateFontGroup(((DTextModel) selected.model).getText(),
					((DTextModel) selected.model).getFontName());
		}

		if (Whiteboard.mode == Whiteboard.SERVER_MODE) {
			Whiteboard.serverSends("S_" + listShapes.indexOf(shape));
		}
	}

	public void updateKnobs() {
		Point[] plist = selected.getKnobs();

		for (int p = 0; p < 4; p++) {
			plist[p].x -= KNOB_SIZE / 2;
			plist[p].y -= KNOB_SIZE / 2;

			knobes[p].model.setX(plist[p].x);
			knobes[p].model.setY(plist[p].y);
		}

		if (selected instanceof DLine) {
			for (int p = 0; p < 2; p++) {
				knobes[2 * p + 1].model.setX(knobes[2 * p].model.getX());
				knobes[2 * p + 1].model.setY(knobes[2 * p].model.getY());
			}
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		for (DShape shape : listShapes)
			shape.draw(g);

		if (Whiteboard.mode != Whiteboard.CLIENT_MODE) {
			if (hasSelected())
				for (int x = 0; x < 4; x++) {
					knobes[x].draw(g);
				}
		}
	}

	public void addShape(DShapeModel dshapeModel) {
		if (dshapeModel instanceof DRectModel) {
			DRect toAdd = new DRect(dshapeModel, this);
			listShapes.add(toAdd);
			whiteboard.addToTable(toAdd);

		} else if (dshapeModel instanceof DOvalModel) {
			DOval toAdd = new DOval(dshapeModel, this);
			listShapes.add(toAdd);
			whiteboard.addToTable(toAdd);

		} else if (dshapeModel instanceof DLineModel) {
			DLine toAdd = new DLine(dshapeModel, this);
			listShapes.add(toAdd);
			whiteboard.addToTable(toAdd);
		}

		else if (dshapeModel instanceof DTextModel) {
			DText toAdd = new DText(dshapeModel, this);
			listShapes.add(toAdd);
			whiteboard.addToTable(toAdd);
		}
	}

	public void mouseClickedTO(MouseEvent e) {
		Point p = e.getPoint();
		boolean shapeFound = false;

		for (DShape shape : listShapes) {
			if (shape.getBounds().contains(p)) {
				changeSelection(shape);
				shapeFound = true;
			}
		}
		for (int x = 0; x < 4; x++) {
			if (knobes[x].getBounds().contains(p)) {
				shapeFound = true;
				movingKnob = knobes[x];
				movingPoint = selected.getKnobs()[x];
				anchorKnob = knobes[((x + 2) % 4)];
				anchorPoint = selected.getKnobs()[((x + 2) % 4)];
				draggingKnob = true;
			}
		}

		if (!shapeFound)
			selected = null;
		paintComponent(this.getGraphics());
	}

	public void repaintShape(DShape shape) {
		repaint(shape.getBounds());
		if (shape == selected) {
			for (int p = 0; p < 4; p++)
				repaint(knobes[p].getBounds());
		}
	}

	public void setFontForSelected(String fontName) {
		if (selected instanceof DText)
			((DText) selected).setFontName(fontName);
	}

	public void WriteToFile(String fileName, String direction) {
		String content = "";
		for (DShape shape : listShapes)
			content += shape.ConvertToString() + "\n";

		File file = new File(direction + "/" + fileName);

		try {
			file.createNewFile();

			FileWriter fileWriter = new FileWriter(file);

			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write(content);
			bufferedWriter.close();
		} catch (IOException ex) {
			System.out.println("Error writing to file '" + fileName + "'");
		}
	}

	public void ReadFromFile(String filename, String direction) {
		File file = new File(direction + "/" + filename);
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;

			while ((text = reader.readLine()) != null) {
				addShapeFromString(text);
				System.out.println(text + "next line");
			}
			paintComponent(this.getGraphics());

		} catch (IOException ex) {
			System.out.println("error");
		}
	}

	public void addShapeFromString(String text) {
		addShape(getShapeFromString(text));
	}

	public DShapeModel getShapeFromString(String text) {
		if (text.charAt(0) == 'R') {
			DRectModel rect = new DRectModel();
			rect.copyTextToShape(text);
			return rect;
		} else if (text.charAt(0) == 'O') {
			DOvalModel oval = new DOvalModel();
			oval.copyTextToShape(text);
			return oval;
		} else if (text.charAt(0) == 'L') {
			DLineModel line = new DLineModel(0, 0, 0, 0);
			line.copyTextToShape(text);
			return line;
		} else if (text.charAt(0) == 'T') {
			DTextModel newText = new DTextModel();
			newText.copyTextToShape(text);
			return newText;
		} else
			return new DShapeModel();
	}

	public void inputChanges(String text) {
		selected.model = getShapeFromString(text);
		paintComponent(this.getGraphics());
	}
}