package theider.log4jxmlview.app;

import javax.swing.JTable;
import javax.swing.table.TableModel;

public class LogRecordListTable extends JTable {

    public LogRecordListTable(TableModel dm) {
        super(dm);
        getColumnModel().getColumn(2).setCellRenderer(new LevelCellRenderer());
    }
    
}
