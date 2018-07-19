# Deep-Walk-4J

Das Ziel dieser Arbeit ist die Erstellung von Embeddings für einen beliebigen
Graphen unter Verwendung von DeepLearning4j und den bereits implementier-
ten DeepWalk[2] Algorithmus. Die generierten Embeddings sollen verwendet
werden, um eine Webapplikation zu realisieren, die die Ähnlichkeiten zwischen
Knoten darstellt. Die Darstellung soll dabei verschiedene Aspekte betrachten,
wie z.B. Darstellung aller Knoten im Vektorraum, Ähnlichkeit zwischen Knoten,
Top K Knoten bzgl. eines Knotens. Als Graph soll ein Social Network Graph
verwendet werden. Weiterhin soll die Ähnlichkeit zwischen Nutzern mit den The-
men verglichen werden für die sie sich interessieren. Diesbezüglich sollen die Fea-
tures für jeden Nutzer verwendet werden und mithilfe des ParagraphVector[1]
Modell ebenfalls als Embedding dargestellt werden. Die Eingabe für das Modell
sind Dokumente. In dem Fall eine Aneinanderreihung der Tags, so dass für jeden
Nutzer ein Embedding generiert werden kann

## Grafische Oberfläche

![alt text](https://raw.githubusercontent.com/Termilion/Deep-Walk-4J/master/Gui.PNG)

### Benutzung

Visualisierung der Paragraph- und DeepWalk Vektoren  
Wählen sie eine der 3 Actionen:  

1. **GlobalView:** Ansicht aller initalisierten Knoten.  
2. **TopK:** Zeigt die top k ähnlichsten Knoten für einen Eingabe Knoten.  
3. **CompareTo:** Vergleicht zwei Knoten miteinander.  

## Installation

```
git clone https://github.com/Termilion/Deep-Walk-4J.git
cd Deep-Walk-4J

wget https://snap.stanford.edu/data/twitter.tar.gz
tar xzf twitter.tar.gz

wget https://snap.stanford.edu/data/twitter.tar.gz
tar xzf twitter.tar.gz

wget https://snap.stanford.edu/data/twitter_combined.txt.gz
gunzip twitter_combined.txt.gz

mvn package
java -jar target/*.jar twitter/ persist/ twitter_combined.txt

```

Die Grafische Benutzeroberfläche und Api Dokumentation ist unter [http://localhost:8080/](http://localhost:8080/) erreichbar.
