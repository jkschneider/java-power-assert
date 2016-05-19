package org.powerassert.synthetic;

import java.util.ArrayList;
import java.util.List;

public class PowerAssert {
	private boolean failEarly = true;
	private boolean showTypes = false;
	private boolean printExprs = false;

	private RecorderListener<Boolean> listener = new RecorderListener<Boolean>() {
		@Override
		public void valueRecorded(RecordedValue recordedValue) {
		}

		@Override
		public void expressionRecorded(RecordedExpression<Boolean> recordedExpr) {
			if(printExprs)
				System.out.println(new ExpressionRenderer(showTypes).render(recordedExpr));
			if(!recordedExpr.getValue() && failEarly) {
				throw new AssertionError("\n\n" + new ExpressionRenderer(showTypes).render(recordedExpr));
			}
		}

		@Override
		public void recordingCompleted(Recording<Boolean> recording) {
			if(!failEarly) {
				List<RecordedExpression<Boolean>> failedExprs = new ArrayList<>();
				for(RecordedExpression<Boolean> expr: recording.getRecordedExprs()) {
					if(!expr.getValue())
						failedExprs.add(expr);
				}
				if(!failedExprs.isEmpty()) {
					ExpressionRenderer renderer = new ExpressionRenderer(showTypes);
					String rendering = "";
					for(int i = failedExprs.size()-1; i >= 0; i--) {
						rendering += "\n\n" + renderer.render(failedExprs.get(i));
					}
					throw new AssertionError(rendering);
				}
			}
		}
	};

	public RecorderListener<Boolean> getListener() {
		return listener;
	}
}
