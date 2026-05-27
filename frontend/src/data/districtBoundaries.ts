export const districtGeoJson = {
  type: "FeatureCollection",
  features: [
    {
      type: "Feature",
      properties: { name: "Downtown" },
      geometry: {
        type: "Polygon",
        coordinates: [
          [
            [-74.015, 40.705],
            [-74.015, 40.72],
            [-73.995, 40.72],
            [-73.995, 40.705],
            [-74.015, 40.705],
          ],
        ],
      },
    },
    {
      type: "Feature",
      properties: { name: "Midtown" },
      geometry: {
        type: "Polygon",
        coordinates: [
          [
            [-74.0, 40.748],
            [-74.0, 40.77],
            [-73.98, 40.77],
            [-73.98, 40.748],
            [-74.0, 40.748],
          ],
        ],
      },
    },
    {
      type: "Feature",
      properties: { name: "Uptown" },
      geometry: {
        type: "Polygon",
        coordinates: [
          [
            [-73.98, 40.775],
            [-73.98, 40.795],
            [-73.955, 40.795],
            [-73.955, 40.775],
            [-73.98, 40.775],
          ],
        ],
      },
    },
    {
      type: "Feature",
      properties: { name: "Waterfront" },
      geometry: {
        type: "Polygon",
        coordinates: [
          [
            [-74.015, 40.7],
            [-74.015, 40.71],
            [-74.0, 40.71],
            [-74.0, 40.7],
            [-74.015, 40.7],
          ],
        ],
      },
    },
    {
      type: "Feature",
      properties: { name: "Industrial" },
      geometry: {
        type: "Polygon",
        coordinates: [
          [
            [-73.995, 40.725],
            [-73.995, 40.74],
            [-73.97, 40.74],
            [-73.97, 40.725],
            [-73.995, 40.725],
          ],
        ],
      },
    },
  ],
};
