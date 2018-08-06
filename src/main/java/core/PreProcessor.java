package core;

import org.deeplearning4j.graph.api.Vertex;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Pre process twitter data from https://snap.stanford.edu/data/egonets-Twitter.html
 */
public class PreProcessor {

    Logger log = Logger.getLogger("core.PreProcessor");
    List<Vertex<String>> verticesList = new ArrayList<>();
    Map<String, Integer> labelToIdMap = new HashMap<>();
    Map<String, Set<String>> combinedReducedFeatsMap = new HashMap();

    /**
     * Preprocess twitter input data.
     * Creates "combined_vertices" and "features/*" files
     * @param in directory to read the twitter data
     * @param out directory to persist the preprocessed files
     * @param edges edges file of the twitter graph
     */
    public PreProcessor(String in, String out, File edges) {
        // skip if already preprocessed
        if( !new File(out+"features/").exists()) {
            log.info("pre processing data");
            File inFolder = new File(in);
            String[] egofeatFiles = inFolder.list(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.endsWith(".egofeat");
                }
            });
            log.info("find egofeats");

            File featDir = new File(out + "features/");
            if (false == featDir.exists() && false == featDir.mkdirs()) {
                System.err.println("Was not able to create directories: " + featDir);
            }

            // collect features for all nodes
            for (String egofeatFile : egofeatFiles) {

                String ego = egofeatFile.substring(0, egofeatFile.indexOf("."));
                log.info("doing egofeat " + ego);

                // id (position) to feature list
                List<String> featnames = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new FileReader(in + ego + ".featnames"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        featnames.add(line.split(" ")[1]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                log.info("loaded featnames for " + ego);

                // collect features of a node from .egofeat
                List<String> reducedEgoFeats = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new FileReader(in + ego + ".egofeat"))) {
                    String line;
                    int idx = 0;
                    while ((line = br.readLine()) != null) {
                        for (String currentFeat : line.split(" ")) {
                            if (currentFeat.equals("1")) {
                                reducedEgoFeats.add(featnames.get(idx));
                            }
                            idx++;
                        }
                        if (combinedReducedFeatsMap.containsKey(ego)) {
                            combinedReducedFeatsMap.get(ego).addAll(reducedEgoFeats);
                        } else {
                            combinedReducedFeatsMap.put(ego, new HashSet<>(reducedEgoFeats));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // collect features of a node from .feat
                try (BufferedReader br = new BufferedReader(new FileReader(in + ego + ".feat"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] splitted = line.split(" ");
                        String nodeId = splitted[0];
                        int idx = 0;
                        boolean firstinit = !combinedReducedFeatsMap.containsKey(nodeId);
                        for (int i = 1; i < splitted.length; i++) {
                            if (splitted[i].equals("1")) {
                                if (firstinit) {
                                    combinedReducedFeatsMap.put(nodeId, new HashSet<>(Arrays.asList(featnames.get(idx))));
                                    firstinit = false;
                                } else {
                                    combinedReducedFeatsMap.get(nodeId).add(featnames.get(idx));
                                }
                            }
                            idx++;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            // Write preprocessed paragraph files
            for (String key : combinedReducedFeatsMap.keySet()) {
                try (FileWriter fw = new FileWriter(out + "features/" + key)) {
                    List<String> sort = new ArrayList<>(combinedReducedFeatsMap.get(key));
                    Collections.sort(sort);
                    fw.write(String.join(" ", sort));
                    fw.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                log.info("reduced feats for " + key);
            }


        } else {
            log.info("skip preprocessing feats");
        }


        // Vertices PrePro
        Set<String> verticesLabelSet = new HashSet<>();
        File vertFile = new File(out+"combined_vertices");
        if(!vertFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(edges))) {
                String line;
                int idx = 0;
                while ((line = br.readLine()) != null) {
                    verticesLabelSet.add(line.split(" ")[0]);
                    verticesLabelSet.add(line.split(" ")[1]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try (BufferedReader br = new BufferedReader(new FileReader(vertFile))) {
                String line;
                int idx = 0;
                while ((line = br.readLine()) != null) {
                    verticesLabelSet.add(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // Generate vertices and label to id map
        List<String> verticesLabelList  = new ArrayList<>(verticesLabelSet);
        Collections.sort(verticesLabelList);
        int idx = 0;
        for(String vertexLabel : verticesLabelList) {
            labelToIdMap.put(vertexLabel, idx);
            verticesList.add(new Vertex<>(idx++, vertexLabel));
        }

        //Write vertices file
        if(!vertFile.exists()) {
            try {
                FileWriter fw = new FileWriter(vertFile);
                for(String vertexLabel : verticesLabelList) {
                    fw.write(vertexLabel+"\n");
                }
                fw.flush();
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Getter vertices list
     * @return list of all vertices
     */
    public List<Vertex<String>> getVertices() {
        return verticesList;
    }

    /**
     * Getter Map containing label -> id
     * @return map from label to internal id
     */
    public Map<String,Integer> getLabelToIdMap() {
        return labelToIdMap;
    }
}
