package locus.api.objects.extra;

import java.io.IOException;
import java.io.InvalidObjectException;

import locus.api.objects.GeoData;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

public class Circle extends GeoData {

	// center location
	private Location loc;
	// radius of circle
	private float radius;
	// draw as precise geodetic circle
	private boolean drawPrecise;
	
	/**
	 * Empty constructor for storable object only,
	 * do not use directly
	 */
	public Circle() {
		super();
	}
	
	public Circle(Location loc, float radius) throws IOException {
		this(loc, radius, false);
	}
	
	public Circle(Location loc, float radius, boolean drawPrecise) throws IOException {
		super();
		this.loc = loc;
		this.radius = radius;
		this.drawPrecise = drawPrecise;
		checkData();
	}
	
	public Circle(byte[] data) throws IOException {
		super(data);
		checkData();
	}
	
	private void checkData() throws InvalidObjectException {
		if (loc == null) {
			throw new InvalidObjectException("Location cannot be 'null'");
		}
		// store radius
		if (radius <= 0.0f) {
			throw new InvalidObjectException("radius have to be bigger then 0");
		}
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public float getRadius() {
		return radius;
	}
	
	public boolean isDrawPrecise() {
		return drawPrecise;
	}
	
	public void setDrawPrecise(boolean drawPrecise) {
		this.drawPrecise = drawPrecise;
	}

	// STORABLE PART
	
	@Override
	protected int getVersion() {
		return 1;
	}

	@Override
	protected void readObject(int version, DataReaderBigEndian dr) throws IOException {
		
		// GEODATA PART
		
		id = dr.readLong();
		name = dr.readString();
		readExtraData(dr);
		readStyles(dr);

		// PRIVATE PART
		
		loc = new Location(dr);
		radius = dr.readFloat();
		drawPrecise = dr.readBoolean();
		
		// version 1 extension
		if (version >= 1) {
			timeCreated = dr.readLong();
		}
	}

	@Override
	protected void writeObject(DataWriterBigEndian dw) throws IOException {
		
		// GEODATA PART
		
		dw.writeLong(id);
		dw.writeString(name);
		writeExtraData(dw);
		writeStyles(dw);

		// PRIVATE PART
		
		loc.write(dw);
		dw.writeFloat(radius);
		dw.writeBoolean(drawPrecise);
		
		// version 1 extension
		dw.writeLong(timeCreated);
	}

	@Override
	public void reset() {
		loc = null;
		radius = 0.0f;
		drawPrecise = false;
		
		// V1
		timeCreated = System.currentTimeMillis();
	}
}
