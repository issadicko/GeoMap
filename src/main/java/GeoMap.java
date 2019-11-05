import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.*;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.event.MapMouseListener;
import org.geotools.swing.tool.CursorTool;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class GeoMap extends JMapFrame{

    private ArrayList<LPoint> liste;
    private static int plugPlus = 0;
    private MapPointSelectionObserver observer;

    public GeoMap(ArrayList<LPoint> liste){

        super(new MapContent());

        this.liste  = liste;
        this.enableTool(Tool.POINTER, Tool.RESET, Tool.ZOOM, Tool.PAN);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.enableStatusBar(false);

        addPoints(liste);

        setPaneCursor();

        drawPolyGone();
    }

    private void setPaneCursor(){
        //Initialisation des évenements
        this.getMapPane().addMouseListener(new MapMouseListener() {
            @Override
            public void onMouseClicked(MapMouseEvent ev) {
                plugPlus = 0;
                LPoint point = getPointIndex(ev.getWorldPos().getDirectPosition().getCoordinate(), liste);

                if (point != null && observer != null) observer.onPointSelected(point);
            }

            @Override
            public void onMouseDragged(MapMouseEvent ev) {

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

        for (LPoint p : liste){
            coords.add(new Coordinate(p.getLatitude(), p.getLongitude()));
        }

        Style style = SLD.createPolygonStyle(Color.BLACK, Color.CYAN, (float) 0.01);

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
