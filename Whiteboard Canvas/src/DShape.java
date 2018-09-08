

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class DShape implements ModelListener {

	protected static final double STARTING_SIZE = 1.0;
	protected Font computedFont;
	protected DShapeModel model;
	protected Canvas canvas;

	public DShapeModel getModel() {
		return model;
	}

	public DShape(String text) {

	}

	public DShape(DShapeModel model, Canvas canvas) {
		this.model = model;
		this.canvas = canvas;
		model.addChangeListener(this);
	}

	public Rectangle getBounds() {
		return new Rectangle(0, 0, 0, 0);
	}

	public void draw(Graphics g) {
	}

	public void modelChanged(DShapeModel model) {
		if (this.model == model) {
			canvas.repaintShape(this);
			if (Whiteboard.mode == Whiteboard.SERVER_MODE) {
				Whiteboard.serverSends("C_" + this.ConvertToString());
			}
		}
	}

	public Point[] getKnobs() {
		Point[] list = new Point[4];
		list[0] = new Point(model.getX(), model.getY());
		list[1] = new Point(model.getX() + model.getWidth() - 1, model.getY());
		list[2] = new Point(model.getX() + model.getWidth() - 1, model.getY() + model.getHeight() - 1);
		list[3] = new Point(model.getX(), model.getY() + model.getHeight() - 1);

		return list;
	}

	public String ConvertToString() {
		return "";
	}
}

class DRect extends DShape // draws square by default
{

	public DRect(DShapeModel model, Canvas canvas) {
		super(model, canvas);
	}

	public void draw(Graphics g) {
		super.draw(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(model.getColor());
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setPaint(model.getColor());
		g2.fill(new Rectangle2D.Double(model.getX(), model.getY(), model.getWidth(), model.getHeight()));
	}

	public Rectangle getBounds() {
		return new Rectangle(model.getX(), model.getY(), model.getWidth(), model.getHeight());
	}

	public String ConvertToString() {
		return model.ConvertToString();
	}
}

class DOval extends DShape // draws circle object
{

	public DOval(DShapeModel model, Canvas canvas) {
		super(model, canvas);
	}

	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(model.getColor());
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setPaint(model.getColor());
		g2.fillOval(model.getX(), model.getY(), model.getWidth(), model.getHeight());
	}

	public Rectangle getBounds() {
		return new Rectangle(model.getX(), model.getY(), model.getWidth(), model.getHeight());
	}

	public String ConvertToString() {
		return model.ConvertToString();
	}

}

class DLine extends DShape // draws a line with 2 knobs
{

	public DLine(DShapeModel model, Canvas canvas) {
		super((DLineModel) model, canvas);
		this.model = new DLineModel(model.x, model.y, model.width, model.height);
	}

	public DLineModel getModel() {
		return (DLineModel) model;
	}

	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(model.getColor());
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setPaint(model.getColor());
		g2.draw(new Line2D.Double(getModel().p1.x, getModel().p1.y, getModel().p2.x, getModel().p2.y));
	}

	public Rectangle getBounds() {
		Rectangle rect = new Rectangle(getModel().p1);
		rect.add(getModel().p2);
		return rect;
	}

	public Point[] getKnobs() {
		Point[] list = new Point[4];
		list[0] = new Point(getModel().p1.x, getModel().p1.y);
		list[1] = new Point(getModel().p1.x, getModel().p1.y);
		list[2] = new Point(getModel().p2.x, getModel().p2.y);
		list[3] = new Point(getModel().p2.x, getModel().p2.y);

		return list;
	}

	public String ConvertToString() {
		return "L_" + String.format("%04d", getModel().p1.x) + "_" + String.format("%04d", getModel().p1.y) + "_"
				+ String.format("%04d", getModel().p2.x) + "_" + String.format("%04d", getModel().p2.y) + "_"
				+ String.format("%04d", getModel().getX()) + "_" + String.format("%04d", getModel().getY()) + "_"
				+ String.format("%04d", getModel().getWidth()) + "_" + String.format("%04d", getModel().getHeight())
				+ "_[C]" + String.valueOf(getModel().color.getRGB()) + "[C/]";
	}
}

class DText extends DShape // draws text with 4 knobs
{
	public DText(DShapeModel model, Canvas canvas) {
		super(model, canvas);
	}

	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(model.getColor());
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setPaint(model.getColor());

		Font calculatedFont = computeFont(g);
		g.setFont(calculatedFont);
		g.drawString(getModel().getText(), getModel().getX(), getModel().getY() + getModel().fontSize);
	}

	public DTextModel getModel() {
		return (DTextModel) model;
	}

	public Rectangle getBounds() {
		Rectangle rect = new Rectangle(getModel().x, getModel().y, getModel().length, getModel().fontSize);
		return rect;
	}

	public Point[] getKnobs() {
		Point[] list = new Point[4];
		list[0] = new Point(getModel().x, getModel().y);
		list[1] = new Point(getModel().x, getModel().y + getModel().fontSize);
		list[2] = new Point(getModel().x + getModel().length, getModel().y + getModel().fontSize);
		list[3] = new Point(getModel().x + getModel().length, getModel().y);

		return list;
	}

	public void setFontName(String newName) {
		if (newName.equals(getModel().getFontName()))
			return;
		getModel().setFontName(newName);
	}

	public Font computeFont(Graphics g) {
		double size = STARTING_SIZE;
		double lastSize = size;
		while (true) {
			computedFont = new Font(getModel().getFontName(), Font.PLAIN, (int) size);
			if (computedFont.getLineMetrics(getModel().getText(), ((Graphics2D) g).getFontRenderContext())
					.getHeight() > getModel().getHeight())
				break;
			lastSize = size;
			size = (size * 1.001) + 1;
		}
		computedFont = new Font(getModel().getFontName(), Font.PLAIN, (int) lastSize);
		return computedFont;
	}

	public String ConvertToString() {
		return model.ConvertToString();
	}

}
