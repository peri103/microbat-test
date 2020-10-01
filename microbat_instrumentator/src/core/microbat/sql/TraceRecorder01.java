package microbat.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import microbat.handler.xml.VarValueXmlWriter;
import microbat.model.BreakPoint;
import microbat.model.trace.StepVariableRelationEntry;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import sav.common.core.utils.CollectionUtils;

/**
 * @author knightsong
 *
 */
public class TraceRecorder01 extends SqliteServer {


	public static final int READ = 1;
	public static final int WRITE = 2;
	
	/**
	 * @param dbPath
	 */
	public TraceRecorder01(String dbPath) {
		super(dbPath);
	}
	
	public void storeTrace(Trace trace) throws SQLException {
		Connection conn = null;
		List<AutoCloseable> closables = new ArrayList<AutoCloseable>();
		try {
			conn=getConnection();
			conn.setAutoCommit(false);
			insertTrace(trace, conn, closables);
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			rollback(conn);
			throw e;
		} finally {
			closeDb(conn, closables);
		}
	}

	public String insertTrace(Trace trace, Connection conn, List<AutoCloseable> closables) throws SQLException {
		return insertTrace(trace, null, null, null, null, conn, closables);
	}
  
	/**
	 *  TODO add insert Run table 
	 *  So,there run id is not clear ,and thread_name is also no clear
	 *  TODO add Variable to tb
	 */
	public String insertTrace(Trace trace, String projectName, String projectVersion, String launchClass,
			String launchMethod, Connection conn, List<AutoCloseable> closables) throws SQLException {
		PreparedStatement ps;
		String sql = "INSERT INTO Trace ("
				+"run_id, trace_id,thread_name,generated_time) " + "VALUES (?, ?, ?,?)";
		ps = conn.prepareStatement(sql);
		closables.add(ps);
		int idx = 1;
		ps.setInt(idx++, -1);
		String traceId=getUUID();
		ps.setString(idx++, traceId);
		ps.setString(idx++,"main");
		ps.setTimestamp(idx++, new Timestamp(System.currentTimeMillis()));
		ps.execute();
		insertSteps(traceId, trace.getExecutionList(), conn, closables);
		insertStepVariableRelation(trace, traceId, conn, closables);
		return traceId;
	}
  
	private void insertSteps(String traceId, List<TraceNode> exectionList, Connection conn, List<AutoCloseable> closables)
			throws SQLException {
		String sql = "INSERT INTO Step (trace_id, step_order, control_dominator, step_in, step_over, invocation_parent, loop_parent,"
				+ "location_id, read_vars, written_vars) VALUES (?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		closables.add(ps);
		insertLocation(traceId, exectionList, conn, closables);
		int count = 0;
		for (int i = 0; i < exectionList.size(); i++) {
			TraceNode node = exectionList.get(i);
			int idx = 1;
			ps.setString(idx++, traceId);
			ps.setInt(idx++, node.getOrder());
			setNodeOrder(ps, idx++, node.getControlDominator());
			setNodeOrder(ps, idx++, node.getStepInNext());
			setNodeOrder(ps, idx++, node.getStepOverNext());
			setNodeOrder(ps, idx++, node.getInvocationParent());
			setNodeOrder(ps, idx++, node.getLoopParent());
			ps.setString(idx++, node.getDeclaringCompilationUnitName()+"_"+node.getLineNumber());
			ps.setString(idx++, generateXmlContent(node.getReadVariables()));
			ps.setString(idx++, generateXmlContent(node.getWrittenVariables()));
			ps.addBatch();
			if (++count == BATCH_SIZE) {
				ps.executeBatch();
				count = 0;
			}
		}
		if (count > 0) {
			ps.executeBatch();
		}
	}
	//TODO value_string is not true instance
	private void insertStepVariableRelation(Trace trace, String traceId, Connection conn, List<AutoCloseable> closables)
			throws SQLException {
		String sql = "INSERT INTO StepVariableRelation (var_id,step_order,rw,value_string,trace_id) VALUES (?, ?, ?, ?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		closables.add(ps);
		int count = 0;
		for (StepVariableRelationEntry entry : trace.getStepVariableTable().values()) {
			for (TraceNode node : entry.getProducers()) {
				int idx = 1;
				ps.setString(idx++, entry.getVarID());
				ps.setInt(idx++, node.getOrder());
				ps.setInt(idx++, WRITE);
				ps.setString(idx++, node.getWrittenVariables().toString());
				ps.setString(idx++, traceId);
				ps.addBatch();
			}
			for (TraceNode node : entry.getConsumers()) {
				int idx = 1;
				ps.setString(idx++, entry.getVarID());
				ps.setInt(idx++, node.getOrder());
				ps.setInt(idx++, READ);
				ps.setString(idx++, node.getReadVariables().toString());
				ps.setString(idx++, traceId);
				ps.addBatch();
			}
			if (++count >= BATCH_SIZE) {
				ps.executeBatch();
				count = 0;
			}
		}
		if (count > 0) {
			ps.executeBatch();
		}
	}
	
	private void insertLocation(String traceId, List<TraceNode> nodes, Connection conn,
			List<AutoCloseable> closables) throws SQLException {
		String sql = "INSERT INTO Location (location_id,trace_id, class_name, line_number, is_conditional, is_return) "
				+ "VALUES (?,?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		closables.add(ps);
		int count = 0;
		HashSet<BreakPoint> set =getLoactionSet(nodes);
		List<String> ids = new ArrayList<>();
		for (BreakPoint location : set) {
			int idx = 1;
			String locationId=getUUID();
			ps.setString(idx++,location.getDeclaringCompilationUnitName()+"_"+location.getLineNumber());
			ps.setString(idx++, traceId);
			ps.setString(idx++, location.getDeclaringCompilationUnitName());
			ps.setInt(idx++, location.getLineNumber());
			ps.setBoolean(idx++, location.isConditional());
			ps.setBoolean(idx++, location.isReturnStatement());
			ids.add(locationId);
			ps.addBatch();
			if (++count == BATCH_SIZE) {
				ps.executeBatch();
				count = 0;
			}
		}
		if (count > 0) {
			ps.executeBatch();
		}

		if (ids.size() != set.size()) {
			throw new SQLException("Number of locations is incorrect!");
		}
		//insertControlScope(traceId, result, conn, closables);
		//insertLoopScope(traceId, result, conn, closables);
	}
	
	private HashSet<BreakPoint> getLoactionSet(List<TraceNode> list) {
		HashSet<BreakPoint> set= new HashSet<>();
		for(TraceNode node:list) {
			set.add(node.getBreakPoint());
		}
		return set;
	}

	protected String generateXmlContent(Collection<VarValue> varValues) {
		if (CollectionUtils.isEmpty(varValues)) {
			return null;
		}
		return VarValueXmlWriter.generateXmlContent(varValues);
	}

	private void setNodeOrder(PreparedStatement ps, int idx, TraceNode node) throws SQLException {
		if (node != null) {
			ps.setInt(idx, node.getOrder());
		} else {
			ps.setNull(idx, java.sql.Types.INTEGER);
		}
	}

}
