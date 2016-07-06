package com.goEuro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JSONAPI 
{
	/*
	 * This function takes the endpoint URL, opens a connection between the program and the URL by calling openConnection to the endpoint URL.
	 * if the connection was successful, creates an InputStreamReader with stream from the connection. 
	 * Buffer the stream in a bufferedReader object, append the characters to a stringBuilder and return the string. 
	 */
	public static String queryString(String endpoint)
	{
		StringBuilder sb = new StringBuilder();
		URLConnection urlConn = null;
		InputStreamReader input = null;
		
		try
		{
			URL url = new URL(endpoint);
			urlConn = url.openConnection();
			if(urlConn !=null)
				urlConn.setReadTimeout(60*1000);
			
			// Check if the connection was successful. 
			if(urlConn !=null && urlConn.getInputStream() != null)
			{
				input = new InputStreamReader(urlConn.getInputStream(),Charset.defaultCharset());
				BufferedReader buff = new BufferedReader(input);
				
				if(buff != null)
				{
					int c;
					while((c=buff.read()) != -1)
					{
						sb.append((char)c);
					}
					buff.close();
				}
			}
			input.close();	
		}
		catch(Exception e)
		{
			throw new RuntimeException("Exception while calling: " + endpoint,e);
		}
		return sb.toString();
	}
	
	
	public static void main(String[] args)
	{
		// Check if the user didn't give any argument i.e. no city name entered. 
		if (args.length == 0)
		{
			System.out.println("You must enter at least one argument");
			System.exit(0);
		}
		
		String cityName =args[0];		
		String end = "http://api.goeuro.com/api/v2/position/suggest/en/";
		
		String endPoint = end + cityName.replaceAll("\\s", "");
				
		// query JSON Data from endpoint URL by calling the queryString method. 
		String jsonDoc = queryString(endPoint);
		try
		{
			JSONArray jsonArray = new JSONArray(jsonDoc);		// create a jsonarray from the returned string
			
			// if the array is empty, exit. 
			if(jsonArray.length() == 0)
			{
				System.out.println("No match found for city: " + cityName);
				System.exit(0);
			}
			
			// write data to csv using a FileWriter object. 
			File csvFile = new File("results.csv");
			
			final String CSV_HEADER = "_id, name, type, latitude, longitude";
		    final String COMMA_DELIMITER = ",";
		    final String NEW_LINE_SEPARATOR = "\n";
			
			FileWriter writer = null;
			try 
			{
				writer = new FileWriter(csvFile);
				
				// first append the header for the CSV file and a new line. 
				writer.append(CSV_HEADER.toString());
				writer.append(NEW_LINE_SEPARATOR);
				
				/*
				 *  for each element in the JSON array, populate each row of the CSV file.
				 *  separate each column by a comma and each row by a new line separator.
				 */
				for ( int i = 0; i < jsonArray.length();i++)
				{
					JSONObject jsonObj = jsonArray.getJSONObject(i);
					writer.append(String.valueOf(jsonObj.getInt("_id")));
					writer.append(COMMA_DELIMITER);
					writer.append(String.valueOf(jsonObj.getString("name")));
					writer.append(COMMA_DELIMITER);
					writer.append(String.valueOf(jsonObj.getString("type")));
					writer.append(COMMA_DELIMITER);
					
					// Now get the inner object of geo_location
					writer.append(String.valueOf(jsonObj.getJSONObject("geo_position").getDouble("latitude")));
					writer.append(COMMA_DELIMITER);
					writer.append(String.valueOf(jsonObj.getJSONObject("geo_position").getDouble("longitude")));
					writer.append(NEW_LINE_SEPARATOR);

				}
	            System.out.println("CSV file was created successfully !!!");

			} 
			catch (IOException e) 
			{
	            System.out.println("Error in FileWriter !!!");
				e.printStackTrace();
			}
			finally
			{
				try 
				{
					writer.flush();
					writer.close();
				} catch (IOException e)
				{
		            System.out.println("Error while flushing/closing FileWriter !!!");
					e.printStackTrace();
				}
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
	}
}
