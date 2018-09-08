

import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;


public class WhiteboardTableModel  extends AbstractTableModel implements ModelListener
{
	public static final String[] DEFAULT_COLUMNS = {"X", "Y", "Width", "Height"};//, "ID"};
    
    private ArrayList<DShapeModel> models;
    
    public WhiteboardTableModel() 
    {
        super();
        models = new ArrayList<DShapeModel>();
    }
    
    public void addModel(DShapeModel model) 
    {
        models.add(0, model);
        model.addListener(this);
        fireTableDataChanged();
    }
    
    public void removeModel(DShapeModel model) 
    {
        model.removeListener(this);
        models.remove(model);
        fireTableDataChanged();
    }
    
    public void moveModelToBack(DShapeModel model) 
    {
        if(!models.isEmpty() && models.remove(model))
            models.add(model);
        fireTableDataChanged();
    }
    
    public void moveModelToFront(DShapeModel model) 
    {
        if(!models.isEmpty() && models.remove(model))
            models.add(0, model);
        fireTableDataChanged();
    }
    
    public void clear() 
    {
        models.clear();
        fireTableDataChanged();
    }
    
    public int getRowForModel(DShapeModel model) 
    {
        return models.indexOf(model);
    }
    
    public String getColumnName(int col) 
    {
        return DEFAULT_COLUMNS[col];
    }
    
    public int getRowCount() 
    {
        return models.size();
    }

	public int getColumnCount() 
	{
        return DEFAULT_COLUMNS.length;
    }
	
	public Object getValueAt(int rowIndex, int columnIndex) 
	{
        Rectangle bounds = models.get(rowIndex).getBounds();
        switch(columnIndex) 
        {
            case 0:  return bounds.x;
            case 1:  return bounds.y;
            case 2:  return bounds.width;
            case 3:  return bounds.height;
            default: return null;
        }
    }
	
	 public boolean isCellEditable(int row, int col) 
	 {
	        return false;
	 }

	@Override
	public void modelChanged(DShapeModel model) 
	{
        int index = models.indexOf(model);
        fireTableRowsUpdated(index, index);
    }

}
