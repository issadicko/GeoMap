import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.action.ZoomInAction;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.event.MapMouseListener;
import org.geotools.swing.tool.ZoomInTool;
import org.geotools.swing.tool.ZoomOutTool;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class GeoMap extends JMapFrame{

    private ArrayList<LPoint> bordure;
    private ArrayList<LPoint> allPoints;
    private Point panePos;

    private static int plugPlus = 0;
    private MapPointSelectionObserver observer;

    private JButton zoomIn, zoomOut;

    public GeoMap(ArrayList<LPoint> bordure){
        super(new MapContent());
        allPoints = new ArrayList<>();

        allPoints.addAll(bordure);

        this.bordure = bordure;
        this.enableTool(Tool.RESET, Tool.SCROLLWHEEL, Tool.ZOOM, Tool.POINTER);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addPoints(bordure);

        setPaneCursor();

        zoomIn = new JButton("+");
        zoomOut = new JButton("-");
        zoomIn.setFont(new Font("Serif", Font.PLAIN, 20));
        zoomOut.setFont(new Font("Serif", Font.PLAIN, 20));

        getMapPane().add(zoomOut);
        getMapPane().add(zoomIn);

        initZoomEvent();

        drawPolyGone();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

    }

    private void initZoomEvent() {
        zoomIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                Rectangle paneArea = ((JComponent) getMapPane()).getVisibleRect();

                double scale = getMapPane().getWorldToScreenTransform().getScaleX();
                double newScale = scale * 1.5;

                Envelope2D newMapArea = new Envelope2D();
                newMapArea.setFrameFromCenter((getWidth()/10.), (getWidth()/10.) ,(getWidth()/2.) - 0.5d * paneArea.getWidth() / newScale,
                         (getHeight()/2.) + 0.5d * paneArea.getHeight() / newScale);
                getMapPane().setDisplayArea(newMapArea);

            }
        });

        zoomOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                getMapPane().setCursorTool(new ZoomOutTool());

                try {

                    Robot robot = new Robot();
                    robot.mouseMove((getMapPane().getWidth()/2), (int) (getHeight()/(1.8)));
                    robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                    robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);

                } catch (AWTException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void setPaneCursor(){
        //Initialisation des évenements
        this.getMapPane().addMouseListener(new MapMouseListener() {
            @Override
            public void onMouseClicked(MapMouseEvent ev) {
                plugPlus = 0;
                LPoint point = getPointIndex(ev.getWorldPos().getDirectPosition().getCoordinate(), allPoints);

                if (point != null && observer != null) observer.onPointSelected(point);
            }

            @Override
            public void onMouseDragged(MapMouseEvent ev) {

                Point pos = ev.getPoint();
                if (!pos.equals(panePos)) {
                    getMapPane().moveImage(pos.x - panePos.x, pos.y - panePos.y);
                    panePos = pos;
                }

            }

            @Override
            public void onMouseEntered(MapMouseEvent ev) {

            }

            @Override
            public void onMouseExited(MapMouseEvent ev) {

            }

            @Override
            public void onMouseMoved(MapMouseEvent ev) {

            }

            @Override
            public void onMousePressed(MapMouseEvent ev) {
                System.out.println(ev.getClickCount());
                panePos = ev.getPoint();
            }

            @Override
            public void onMouseReleased(MapMouseEvent ev) {

            }

            @Override
            public void onMouseWheelMoved(MapMouseEvent ev) {

            }
        });
    }

    private LPoint getPointIndex(double[] position2D, ArrayList<LPoint> liste){
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(4 + plugPlus);
        ArrayList<LPoint> subListe = new ArrayList<>();

        for (LPoint lPoint : liste){

            if (format.format(position2D[0]).equals(format.format(lPoint.getLatitude())) && format.format(position2D[1]).equals(format.format(lPoint.getLongitude())))
            {
                subListe.add(lPoint);
            }
        }

        if (subListe.size() == 0) return null;

        if (subListe.size() == 1) return subListe.get(0);
        plugPlus++;
        return getPointIndex(position2D, subListe);

    }

    private void addPoints(ArrayList<LPoint> liste){

        MapContent map = getMapContent();

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("MyFeatureType");
        builder.setCRS( DefaultGeographicCRS.WGS84 );
        builder.add("location", Point.class);

        SimpleFeatureType pointtype = null;


        SimpleFeatureCollection collectionPoints = FeatureCollections.newCollection();

        for (LPoint lpoint : liste){

            try {
                pointtype = DataUtilities.createType("POINT", "geom:Point,name:String");
            } catch (SchemaException ex) {
                ex.printStackTrace();
                System.exit(0);
            }

            Coordinate coord = new Coordinate(lpoint.getLatitude(), lpoint.getLongitude());

            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            org.locationtech.jts.geom.Point point = geometryFactory.createPoint( coord );

            SimpleFeatureBuilder featureBuilderPoints = new SimpleFeatureBuilder(pointtype);
            featureBuilderPoints.set("name", lpoint.getNom());
            featureBuilderPoints.add(point);
            ((DefaultFeatureCollection)collectionPoints).add(featureBuilderPoints.buildFeature(null));

        }

        Style pointStyle = SLD.createPointStyle("circle", Color.BLACK, Color.BLACK,(float)1 ,(float) 10, "name", null);
        FeatureLayer layer = new FeatureLayer(collectionPoints, pointStyle);

        map.addLayer(layer);
    }

    public void draw(ArrayList<LPoint> liste){
        allPoints.addAll(liste);
        addPoints(liste);
    }

    public static void main(String[] args) {
        ArrayList<LPoint> liste = CSVReader.parse("data.csv");

        ArrayList<LPoint> liste2 = CSVReader.parse2("milieu.csv");

        GeoMap geoMap   = new GeoMap(liste);
        geoMap.draw(liste2);
        geoMap.setVisible(true);


        geoMap.setPointSelectionListener(new MapPointSelectionObserver() {
            @Override
            public void onPointSelected(LPoint point) {
                JOptionPane.showMessageDialog(null, "Longitude : "+point.getLongitude()+"; Latitude : "+point.getLatitude());
            }
        });

    }

    public void setPointSelectionListener(MapPointSelectionObserver observer){
        this.observer = observer;
    }

    private void drawPolyGone(){

        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();
        ArrayList<Coordinate> coords = new ArrayList<>();

        for (LPoint p : bordure){
            coords.add(new Coordinate(p.getLatitude(), p.getLongitude()));
        }

        Style style = SLD.createPolygonStyle(Color.BLACK, Color.CYAN, (float) 0.03);

        Geometry polygon = factory.createPolygon(coords.toArray(Coordinate[]::new));

        addGeometry(polygon, style);
    }

    private void addGeometry(Geometry geom, Style style) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Location");
        builder.setCRS(DefaultGeographicCRS.WGS84);
        builder.add("location", Geometry.class);

        SimpleFeatureType type = builder.buildFeatureType();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
        featureBuilder.add(geom);

        SimpleFeature feature = featureBuilder.buildFeature(null);

        DefaultFeatureCollection simpleFeatures = new DefaultFeatureCollection();
        simpleFeatures.add(feature);

        Layer layer = new FeatureLayer(simpleFeatures, style);
        this.getMapContent().addLayer(layer);

    }

}
