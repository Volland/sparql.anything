package com.github.sparqlanything.engine;

import com.github.sparqlanything.model.*;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.main.QC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class QueryIterSlicer extends QueryIter {

	private QueryIterator current = null;
	private final Iterator<Slice> iterator;
	private Properties p;
	private final ExecutionContext execCxt;
	private final String resourceId;
	private final OpService opService;
	private static final Logger logger = LoggerFactory.getLogger(QueryIterSlicer.class);
	final private Slicer slicer;
	final List<Binding> elements;
	private final QueryIterator input;

	public QueryIterSlicer(ExecutionContext execCxt, QueryIterator input, Triplifier t, Properties properties, OpService opService) throws TriplifierHTTPException, IOException {
		super(execCxt);
		slicer = (Slicer) t;
		this.p = properties;
		final Iterable<Slice> it = slicer.slice(p);
		this.input = input;

		elements = new ArrayList<>();
		while (input.hasNext()) {
			elements.add(input.nextBinding());
		}

		this.iterator = it.iterator();
		this.execCxt = execCxt;
		this.resourceId = Triplifier.getResourceId(p);
		this.opService = opService;
	}

	@Override
	protected boolean hasNextBinding() {
		logger.trace("hasNextBinding? ");
		logger.debug("current: {}", current != null ? current.hasNext() : "null");
		while (current == null || !current.hasNext()) {
			if (iterator.hasNext()) {
				Slice slice = iterator.next();
				logger.debug("Executing on slice: {}", slice.iteration());
				// Execute and set current
				FacadeXGraphBuilder builder;
				Integer strategy = PropertyUtils.detectStrategy(p,execCxt);
				if (strategy == 1) {
					logger.trace("Executing: {} [strategy={}]", p, strategy);
					builder = new TripleFilteringFacadeXGraphBuilder(resourceId, opService.getSubOp(), p);
				} else {
					logger.trace("Executing: {} [strategy={}]", p, strategy);
					builder = new BaseFacadeXGraphBuilder(resourceId, p);
				}
				//FacadeXGraphBuilder builder = new TripleFilteringFacadeXGraphBuilder(resourceId, opService.getSubOp(), p);
				slicer.triplify(slice, p, builder);
				DatasetGraph dg = builder.getDatasetGraph();
				logger.debug("Executing on next slice: {} ({})", slice.iteration(), dg.size());
				FacadeXExecutionContext ec = new FacadeXExecutionContext(new ExecutionContext(execCxt.getContext(), dg.getDefaultGraph(), dg, execCxt.getExecutor()));
				logger.trace("Op {}", opService.getSubOp());
				logger.trace("OpName {}", opService.getSubOp().getName());
				/**
				 * input needs to be reset before each execution, otherwise the executor will skip subsequent executions
				 * since input bindings have been flushed!
				 */
				QueryIterator cloned;
				cloned = QueryIterPlainWrapper.create(elements.iterator());
				current = QC.execute(opService.getSubOp(), cloned, ec);
				logger.debug("Set current. hasNext? {}", current.hasNext());
				if (current.hasNext()) {
					logger.trace("Break.");
					break;
				}
			} else {
				logger.trace("Slices finished");
				/**
				 * Input iterator can be closed
				 */
				input.cancel();
				// Make sure the original Op is executed
				// XXX Maybe there is a better qay of doing it?
				ExecutionContext exc = new ExecutionContext(DatasetGraphFactory.create());
				QC.execute(opService.getSubOp(), QueryIterNullIterator.create(exc), exc);
				return false;
			}
		}
		logger.trace("hasNextBinding? {}", current.hasNext());
		return current.hasNext();
	}

	@Override
	protected Binding moveToNextBinding() {
		logger.trace("moveToNextBinding");
		return current.nextBinding();
	}

	@Override
	protected void closeIterator() {
		current.close();
	}

	@Override
	protected void requestCancel() {
		current.cancel();
	}
}
