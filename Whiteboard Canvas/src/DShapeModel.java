

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.LinkedList;

public class DShapeModel 
{
	
	final static int MAX_SIZE = 400;
	final static Color DEFAULT_COLOR = Color.GRAY;		//default color for all shapes
	protected int x,y,width, height;
	protected Color color;
	
	protected  Collection<ModelListener> modelChangeListeners;
	
	
	public DShapeModel()
	{
		x = y = width = height = 0;
		color = Color.GRAY;
		
		modelChangeListeners = new LinkedList<ModelListener>();
	}

	public DShapeModel(int x, int y, int width, int height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		color = DEFAULT_COLOR;
	}

	public int getX() { return x;}
	public int getY() { return y;}
	public int getWidth() { return width;}
	public int getHeight() { return height;}
	public Color getColor() { return color;}

	public boolean setX(int x)
	{
		notifyListenersOfChange();
		this.x = x;
		return true;		
	}

	public boolean setY(int y)
	{
		this.y = y;
		notifyListenersOfChange();
		return true;
	}

	public boolean setWidth(int width)
	{
		notifyListenersOfChange();
		this.width = width;
		return true;
	}
	
	public boolean setHeight(int height)
	{
		notifyListenersOfChange();
		this.height = height;
		return true;
	}

	public boolean setColor(Color color)
	{
		this.color = color;
		notifyListenersOfChange();
		return true;
	}
	
	public void addChangeListener(ModelListener listener)
	{
		modelChangeListeners.add(listener);
	}
	
	public boolean removeChangeListener(ModelListener listener)
	{
		return modelChangeListeners.remove(listener);
	}
	
	protected void notifyListenersOfChange()
	{
		for (ModelListener listener : modelChangeListeners)
			listener.modelChanged(this);
	}
	
	public void addListener(ModelListener listener) 
	{
		modelChangeListeners.add(listener);
    }

    public void removeListener(ModelListener listener)
    {
    	modelChangeListeners.remove(listener);
    }
    
    public Rectangle getBounds() 
    {
    	Rectangle rect = new Rectangle(x,y,width, height);
        return rect;
    }
    
    public String ConvertToString()
    {
    	return "";
    }
	
}

class DRectModel extends DShapeModel
{
	public DRectModel()		// 0 argument constructor
	{
		x = y = width = height = 0;
		color = DEFAULT_COLOR;
	}
	

	public String ConvertToString()
	{
		return "R_" + String.format("%04d", x) + "_" + String.format("%04d", y) 
				+ "_" + String.format("%04d", width) + "_" + String.format("%04d", height)
				+ "_[C]" + String.valueOf(color.getRGB()) + "[C/]";
	}
	
	public void copyTextToShape(String text)
	{
		setX(Integer.parseInt(text.substring(3, 6)));
		setY(Integer.parseInt(text.substring(8, 11)));
		setWidth(Integer.parseInt(text.substring(13, 16)));
		setHeight(Integer.parseInt(text.substring(18, 21)));
		String color = text.substring(text.indexOf("[C]")+ 3, text.indexOf("[C/]"));
		Color c = new Color(Integer.parseInt(color));
		setColor(c);
	}
}

class DOvalModel extends DShapeModel
{
	public DOvalModel()			// 0 argument constructor
	{
		x = y = width = height = 0;
		color = DEFAULT_COLOR;
	}
	
	public String ConvertToString()
	{
		return "O_" + String.format("%04d", x) + "_" + String.format("%04d", y) 
				+ "_" + String.format("%04d", width) + "_" + String.format("%04d", height)
				+ "_[C]" + String.valueOf(color.getRGB()) + "[C/]";
	}
	
	public void copyTextToShape(String text)
	{
		setX(Integer.parseInt(text.substring(3, 6)));
		setY(Integer.parseInt(text.substring(8, 11)));
		setWidth(Integer.parseInt(text.substring(13, 16)));
		setHeight(Integer.parseInt(text.substring(18, 21)));
		String color = text.substring(text.indexOf("[C]")+ 3, text.indexOf("[C/]"));
		Color c = new Color(Integer.parseInt(color));
		setColor(c);

		notifyListenersOfChange();
	}
}


class DLineModel extends DShapeModel
{
	public Point p1,p2;
	
	public DLineModel(int x, int y, int width, int height) 
	{
        super();
        p1 = new Point(x, y);
        p2 = new Point(x + width, y + height);

		notifyListenersOfChange();
    }
	
	public void setP1(Point p)
	{
		p1 = new Point(p);
		notifyListenersOfChange();
	}
	public void setP2(Point p) 
	{
        p2 = new Point(p);
		notifyListenersOfChange();
    }
	
	public Point getP1()
	{
		return p1;
	}
	
	public Point getP2()
	{
		return p2;	
	}
	
	public Rectangle getBounds() 
    {
    	Rectangle rect = new Rectangle(p1);
    	rect.add(p2);
        return rect;
    }
	
	public void copyTextToShape(String text)
	{
		x=y=width=height=0;
		Point point1, point2;
		point1 = new Point(Integer.parseInt(text.substring(3, 6)), Integer.parseInt(text.substring(8, 11)));
		setP1(point1);
		
		point2 = new Point(Integer.parseInt(text.substring(13, 16)), Integer.parseInt(text.substring(18, 21)));
		setP2(point2);
		
		String color = text.substring(text.indexOf("[C]")+ 3, text.indexOf("[C/]"));
		Color c = new Color(Integer.parseInt(color));
		setColor(c);
		Rectangle rect = new Rectangle(point1);
		rect.add(point2);
		

		notifyListenersOfChange();
	}
}


class DTextModel extends DShapeModel
{
	String text, font;
	int fontSize, length;
	
	public DTextModel()		// 0 argument constructor
	{
		x = y = width = height = 0;
		fontSize = height;
		font = "Dialog";
		setText("Hello");

		color = DEFAULT_COLOR;
	}
	
	public void setText(String text)
	{
		this.text = text;
		updateLength();

		notifyListenersOfChange();
	}
	
	public String getText()
	{
		return text;
	}
	
	public void setFontSize(int fontsize)
	{
		this.fontSize = fontsize;
	}

	public void setLength(int length)
	{
		this.length = length;
	}
	
	public void updateLength()
	{
		setLength(text.length() * fontSize/2);		
	}
	
	public void updateFontSize()
	{
		setFontSize(height);
		updateLength();
		notifyListenersOfChange();
		
	}
	
	public boolean setHeight(int height)
	{
		if(this.height != 0 )
			notifyListenersOfChange();
		this.height = height;
		updateFontSize();
		return true;
	}
	
	public String getFontName() 
	{
        return font;
    }
	
	 public void setFontName(String fontName) 
	 {
	        font = fontName;
	        notifyListenersOfChange();
	 }
	 
	public String ConvertToString()
	{
			return "T_" + String.format("%04d", x) + "_" + String.format("%04d", y) 
					+ "_" + String.format("%04d", fontSize) + "_" + String.format("%04d", width)
					+ "_" + String.format("%04d", height) 
					+ "_[C]" + String.valueOf(color.getRGB()) + "[C/]"
					+ "_[T]" + text + "[T/]"
					+ "_[F]" + font + "[F/]";
	}
	
	public void copyTextToShape(String text)
	{
		setX(Integer.parseInt(text.substring(3, 6)));
		setY(Integer.parseInt(text.substring(8, 11)));
		setFontSize(Integer.parseInt(text.substring(13, 16)));

		setWidth(Integer.parseInt(text.substring(18, 21)));
		setHeight(Integer.parseInt(text.substring(23, 26)));
		String color = text.substring(text.indexOf("[C]")+ 3, text.indexOf("[C/]"));
		String newText = text.substring(text.indexOf("[T]")+ 3, text.indexOf("[T/]"));
		String font = text.substring(text.indexOf("[F]")+ 3, text.indexOf("[F/]"));
		Color c = new Color(Integer.parseInt(color));
		setText(newText);
		setFontName(font);
		setColor(c);

		notifyListenersOfChange();
	}
}
