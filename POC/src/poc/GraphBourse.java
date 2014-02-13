package poc;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.io.ADataCollector;
import info.monitorenter.gui.chart.io.RandomDataCollectorOffset;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.views.ChartPanel;
import info.monitorenter.util.Range;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

public class GraphBourse implements Runnable {

	private Chart2D chart;

	private Trace2DLtd trace;

	private CoursBoursier current;

	public GraphBourse(CoursBoursier init) {
		current = init;
		chart = new Chart2D();
		chart.getAxisX().setPaintGrid(true);
		chart.getAxisY().setPaintGrid(true);
		chart.getAxisY().setRangePolicy(new RangePolicyMinimumViewport(new Range(0, init.cotation*2)));
		chart.setGridColor(Color.LIGHT_GRAY);
		trace = new Trace2DLtd(1000);
		trace.setName(init.company);
		trace.setPhysicalUnits("ms", "€");
		trace.setColor(Color.BLUE);
		chart.addTrace(trace);
	}

	public Chart2D getChart() {
		return chart;
	}

	public CoursBoursier getCours() {
		return current;
	}

	public static void main(String[] args) {
		GraphBourse demo = new GraphBourse(new CoursBoursier(0, "", "DEMO",
				100.00));
		Thread thread = new Thread(demo);
		thread.start();

		// Make it visible:
		// Create a frame.
		JFrame frame = new JFrame("Demo");
		// add the chart to the frame:
		frame.getContentPane().add(demo.getChart());
		frame.setSize(400, 300);
		// Enable the termination button [cross on the upper right edge]:
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setVisible(true);

	}

	@Override
	public void run() {
		Timer timer = new Timer(true);
		TimerTask task = new TimerTask() {

			private double m_y = current.cotation;
			private double m_t = 0.45 + Math.random()*0.1;
			@Override
			public void run() {
				// This is just computation of some nice looking value.
				this.m_t = this.m_t + (this.m_t * (Math.random() - 0.5)*0.001);
				double rand = Math.random();
				boolean add = (rand >= m_t || this.m_y < 5.0);
				this.m_y = (add) ? this.m_y + (this.m_y * Math.random()*0.002) : this.m_y
						- (this.m_y * Math.random()*0.002);
				// This is the important thing: Point is added from separate
				// Thread.
				current.time = System.currentTimeMillis();
				current.cotation = this.m_y;
				trace.addPoint(current.time, current.cotation);
			}

		};
		// Every 20 milliseconds a new value is collected.
		timer.schedule(task, 0, 10);
	}

}