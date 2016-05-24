/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
