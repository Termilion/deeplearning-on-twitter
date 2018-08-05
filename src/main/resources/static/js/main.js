/*
    Generierung verschiedener Ansichten der Graphen mit D3js
 */

// Static Windowsizes
var w = 800;
var h = 600;
var padding = 40;

// node colors
var colorMain = d3.color("#ff9100"),
    colorNode= d3.color("#6314cc"),
    colorLink= d3.color("#A70110"),
    colorOther= d3.color("#F4F6FF").darker(0.9);

// input fields
var inputNodeA = document.getElementById("nodeId");
var inputNodeB = document.getElementById("nodeId2");
var inputKInput = document.getElementById("kInput");
var inputmax = document.getElementById("max");

// event listeners for the input (reacts on ENTER)
inputNodeA.addEventListener("keyup", function(event) {
    event.preventDefault();
    if (event.keyCode === 13) {
        change_action();
    }
});

inputNodeB.addEventListener("keyup", function(event) {
    event.preventDefault();
    if (event.keyCode === 13) {
        change_action();
    }
});

inputmax.addEventListener("keyup", function(event) {
    event.preventDefault();
    if (event.keyCode === 13) {
        change_action();
    }
});

inputKInput.addEventListener("keyup", function(event) {
    event.preventDefault();
    if (event.keyCode === 13) {
        change_action();
    }
});

// function call based on chosen operation in action field
// hides/displays the relevant/irrelevant containers for the chosen operation
function change_action() {
    action = document.getElementById('action').value;

    // COMPARE ACTION:
    // local view on two nodes, displaying the relevant features for both nodes for both embeddings
    if (action === "CompareTo") {
        console.log("CompareTo");
        $('.d2').css('display', 'none');
        $('.d3').css('display', 'none');
        $('.d1').css('display', 'block');
        document.getElementById("b1").innerHTML = "";
        document.getElementById("b2").innerHTML = "";
        document.getElementById("log1").innerHTML = "";
        document.getElementById("log2").innerHTML = "";
        var nodeA = document.getElementById('nodeId').value;
        var nodeB = document.getElementById('nodeId2').value;
        compare(nodeA,nodeB);

    // GLOBAL VIEW ACTION:
    // displays global 2D view of all vectors for both embeddings.
    // The 3rd dimension gets displayed through color and size of the node, because of performance reasons
    } else if (action === "GlobalView" ) {
        console.log("GolbalView");
        $('.d1').css('display', 'none');
        $('.d2').css('display', 'none');
        $('.d3').css('display', 'block');
        document.getElementById("b1").innerHTML = "";
        document.getElementById("b2").innerHTML = "";
        document.getElementById("log1").innerHTML = "";
        document.getElementById("log2").innerHTML = "";
        var max = document.getElementById('max').value;

        d3.json("graph/getVectorInfo").then(function (json) {
            loadGlobalGraph(".box1", "vectors/paraVec-3-"+json.paragraphVectors.windowSize+".pv", "log1", max);
            loadGlobalGraph(".box2", "vectors/deepWalk-"+json.deepWalk.walkLength+"-"+json.deepWalk.windowSize+"-3.dw", "log2", max);
        });

    // KTOP ACTION:
    // displays local view of the chosen node with the chosen number of nearest node,
    // respective to the embedding with the relevant features
    } else {
        console.log("kTop");
        $('.d1').css('display', 'none');
        $('.d3').css('display', 'none');
        $('.d2').css('display', 'block');
        document.getElementById("b1").innerHTML = "";
        document.getElementById("b2").innerHTML = "";
        document.getElementById("log1").innerHTML = "";
        document.getElementById("log2").innerHTML = "";
        actionkTop();

    }
}

function actionkTop(){
    var node = document.getElementById("nodeId").value;
    if(action === "CompareTo"){
        console.log(test);
    } else {
        var k = document.getElementById("kInput").value;
        topkParaVec(node, k, ".box1");
        topkDeepWalk(node, k, ".box2");
    }
}

// node comparison for the COMPARE ACTION
function compare(node1, node2) {
    // build the REST query parameter to query for the similarity of the chosen nodes
    var nodeArgs = node1 + ";" + node2;
    d3.json("deepWalk/compare?nodeA=" + node1 +"&nodeB=" + node2).then(function (json) {
        document.getElementById("log2").innerHTML = "<strong>Similarity: </strong>" + json.similar[0].sim;
    });
    d3.json("paragraphVectors/compare?nodeA=" + node1 +"&nodeB=" + node2).then(function (json) {
        document.getElementById("log1").innerHTML = "<strong>Similarity: </strong>" + json.similar[0].sim;
    });
    // Execute REST query for the DeepWalk features
    d3.json("deepWalk/getFriends?label=" + nodeArgs).then(function (json) {
        var n = []; // nodes
        var e = []; // edges

        // add the selected nodes
        n.push({id:node1, type:"main"});
        n.push({id:node2, type:"main"});

        // add features of node 1
        json[0].friends.forEach(function (l) {
            if(!n.map(function (value) { return value.id }).includes(l)) {  // deduplication
                n.push({id:l, type: "outer"});  // add feature node
            }
            e.push({source: node1, target: l}); // add edge between the nodes
        });

        // add features of node 2
        json[1].friends.forEach(function (l) {
            var map = n.map(function (value) { return value.id });
            // add feature node: overlapping (already added) features get marked as "link"
            if(!map.includes(l)) n.push({id:l, type: "outer"});
            else n[map.indexOf(l)] = {id:l, type: "link"};
            e.push({source: node2, target: l}); // add edge between the nodes
        });
        // more relevant nodes need to get added later
        n = sortByType(n);

        // display graph
        var graph = {nodes: n, links: e};
        loadLocalGraph(graph, ".box2");
    });

    // Execute REST query for the DeepWalk features
    d3.json("paragraphVectors/getFeats?label=" + nodeArgs).then(function (json) {
        var n = []; // nodes
        var e = []; // edges

        // add main nodes
        n.push({id:node1, type:"main"});
        n.push({id:node2, type:"main"});

        // add features of node 1
        json[0].feats.forEach(function (l) {
            if(!n.map(function (value) { return value.id }).includes(l)) {  //deduplication
                n.push({id:l, type: "outer"}); // add feature node
            }
            e.push({source: node1, target: l}); // add edge
        });
        // add features of node 2
        json[1].feats.forEach(function (l) {
            var map = n.map(function (value) { return value.id });
            // add feature node: overlapping (already added) features get marked as "link"
            if(!map.includes(l)) n.push({id:l, type: "outer"});
            else n[map.indexOf(l)] = {id:l, type: "link"};
            e.push({source: node2, target: l}); // add edge
        });
        // more relevant nodes need to get added later
        n = sortByType(n);

        // display graph
        var graph = {nodes: n, links: e};
        loadLocalGraph(graph, ".box1");
    });
}

// More important nodes need to get added later, so they will be displayed on top
// ORDER: MAIN more important than LINK or NODE more important than OTHER
function sortByType(n) {
    return n.sort(function (a, b) {
        if(a.type === "main") return 1;
        else {
            if(b.type === "main") return -1;
            else {
                if(a.type === "links" || a.type === "node") return 1;
                else {
                    return -1;
                }
            }
        }
    });
}

// show top k nodes in the deepwalk embedding
function topkDeepWalk(id, k, div){
    // REST query for the top k similar nodes
    d3.json("deepWalk/topK?label="+ id +"&k="+ k).then(function (json) {
        var topKNodes = json.similar;
        var mainNode = json.selection;
        topKNodes.push({label: json.selection, sim: 1});

        // building the query for the feature query
        var nodeArgs = "";
        topKNodes.forEach(function (n) {
            nodeArgs = nodeArgs + n.label + ";";
        });

        // REST query for the features of the top k nodes
        d3.json("deepWalk/getFriends?label=" + nodeArgs).then(function (friends) {
            var n = []; // nodes
            var e = []; // edges
            friends.forEach(function (v) {
                if(v.label === mainNode) n.push({id:v.label, type:"main"}); // marking the main node
                else n.push({id:v.label, type:"node"}); // marking the top k nodes
                v.friends.forEach(function (l) {
                    if(!n.map(function (value) { return value.id }).includes(l)) { // deduplication
                        n.push({id:l, type: "outer"});
                    }
                    e.push({source: v.label, target: l}); // add edge
                });
            });
            // more relevant nodes need to get added later
            n = sortByType(n);

            // display graph
            var graph = {nodes: n, links: e};
            loadLocalGraph(graph, div)
        });
    });
}

// show top k nodes in the paragraphVector embedding
function topkParaVec(id, k, div){
    // REST query for the top k similar nodes
    d3.json("paragraphVectors/topK?label="+ id +"&k="+ k).then(function (json) {
        var topKNodes = json.similar.map(function (d) {
            return {id: d.label, sim: d.sim, type: "node"}
        });
        var mainNode = json.selection;
        topKNodes.push({id: json.selection, sim: 1, type: "main"});

        // building the query for the feature query
        var nodeArgs = "";
        topKNodes.forEach(function (d) {
            nodeArgs = nodeArgs + d.id + ";";
        });
        // REST query for the features of the top k nodes
        d3.json("paragraphVectors/getFeats?label=" + nodeArgs).then(function (feats) {
            var n = []; // nodes
            var e = []; // edges
            feats.forEach(function (v) {
                if(v.label === mainNode) n.push({id:v.label, type:"main"}); // marking the main node
                else n.push({id:v.label, type:"node"}); // marking the top k nodes
                v.feats.forEach(function (l) {
                    if (!n.map(function (value) {return value.id}).includes(l)) { // deduplication
                        n.push({id: l, type:"outer"}); // add node
                    }
                    e.push({source: v.label, target: l}); // add edge
                });
            });
            // more relevant nodes need to get added later
            n = sortByType(n);

            // display graph
            var graph = {nodes: n, links: e};
            loadLocalGraph(graph, div);
        });
    });
}

// loads a local force graph with d3
function loadLocalGraph(graph, div){
    // create the canvas
    var svg = d3.select(div)
        .append("svg")
        .attr("width", w)
        .attr("height", h);

    // create the force layout
    var graphLayout = d3.forceSimulation(graph.nodes)
        .force("charge", d3.forceManyBody().strength(-300))
        .force("center", d3.forceCenter(w / 2, h / 2))
        .force("x", d3.forceX(w / 2).strength(1))
        .force("y", d3.forceY(h / 2).strength(1))
        .force("link", d3.forceLink(graph.links).id(function(d) {return d.id; }).distance(50).strength(1))
        .on("tick", ticked);

    // adjacency list and neighbor function for determining linked nodes (-> focus feature)
    var adjlist = [];
    graph.links.forEach(function(d) {
        adjlist[d.source.index + "-" + d.target.index] = true;
        adjlist[d.target.index + "-" + d.source.index] = true;
    });
    function neigh(a, b) {
        return a == b || adjlist[a + "-" + b];
    }
    // create graph
    var container = svg.append("g");

    // zoom function
    svg.call(
        d3.zoom()
            .scaleExtent([.1, 4])
            .on("zoom", function() { container.attr("transform", d3.event.transform); })
    );

    // generate links between nodes first, so nodes are "on top" of the lines
    var link = container.append("g").attr("class", "links")
        .selectAll("line")
        .data(graph.links)
        .enter()
        .append("line")
        .attr("stroke", "grey")
        .attr("stroke-width", "1px");
    // generate nodes
    var node = container.append("g").attr("class", "nodes")
        .selectAll("g")
        .data(graph.nodes)
        .enter().append('g')
        .attr('class', 'nodes');
    // display nodes as circles
    node.append("circle")
        .attr("r", function (d) {
            // radius is determined by type of node
            if(d.type === "main") return 25;
            else {
                if(d.type === "node") return 13;
                else {
                    if(d.type === "link") return 7;
                    else return 5;
                }
            }
        })
        .attr("fill", function(d) {
            // color is determined by type of node
            if(d.type === "main") return colorMain;
            else {
                if(d.type === "node") return colorNode;
                else {
                    if(d.type === "link") return colorLink;
                    else return colorOther;
                }
            }
        })
        .attr("stroke", "white")
        .attr("stroke-width", "1px");
    // add text label for the nodes last
    node.append("text")
        .attr("dx", 12)
        .attr("dy", ".35em")
        .style("opacity", 0)
        .text(function (d) { return d.id });
    // focus on mouseover
    node.on("mouseover", focus).on("mouseout", unfocus);
    // draggable
    node.call(
        d3.drag()
            .on("start", dragstarted)
            .on("drag", dragged)
            .on("end", dragended)
    );

    function ticked() {
        node.call(updateNode);
        link.call(updateLink);
    }
    function fixna(x) {
        if (isFinite(x)) return x;
        return 0;
    }

    // focus function for clearer view on connected nodes
    // shows labels, hides other nodes
    function focus(d) {
        var index = d3.select(d3.event.target).datum().index;
        node.selectAll("text").style("opacity", function(o) {
            return neigh(index, o.index) ? 1 : 0;
        });
        node.style("opacity", function(o) {
            return neigh(index, o.index) ? 1 : 0.1;
        });
        link.style("opacity", function(o) {
            return o.source.index === index || o.target.index === index ? 1 : 0.1;
        });
    }
    // reset focus
    function unfocus() {
        node.selectAll("text").style("opacity", 0);
        node.style("opacity", 1);
        link.style("opacity", 1);
    }
    function updateLink(link) {
        link.attr("x1", function(d) { return fixna(d.source.x); })
            .attr("y1", function(d) { return fixna(d.source.y); })
            .attr("x2", function(d) { return fixna(d.target.x); })
            .attr("y2", function(d) { return fixna(d.target.y); });
    }
    function updateNode(node) {
        node.attr("transform", function(d) {
            return "translate(" + fixna(d.x) + "," + fixna(d.y) + ")";
        });
    }
    function dragstarted(d) {
        d3.event.sourceEvent.stopPropagation();
        if (!d3.event.active) graphLayout.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
    }
    function dragged(d) {
        d.fx = d3.event.x;
        d.fy = d3.event.y;
    }
    function dragended(d) {
        if (!d3.event.active) graphLayout.alphaTarget(0);
        d.fx = null;
        d.fy = null;
    }
}

// loads a local force graph with d3
function loadGlobalGraph(div, path, log, max) {
    var vectorNodes = []; // nodes
    var isSelected = null; // selection
    // generate canvas
    var svg = d3.select(div)
        .append("svg")
        .attr("width", w)
        .attr("height", h);

    // load the pregenerated embedding data as dsv (delimited by whitespace)
    d3.text(path).then(function (d) {
        var vectors = d.split("\n");
        // maximum displayed nodes configurable
        max = Math.min(vectors.length, max);
        // load nodes
        var i;
        for(i = 0; i < max; i++){
            var vec = vectors[i];
            var values = vec.split("\t");
            if(values.length >= 4) vectorNodes.push({id: values[0], x: values[1], y: values[2], z: values[3]});
        }
        // get maximum values for setting the scale
        var xmax = Math.max.apply(Math, vectorNodes.map(function(d){return d.x;}));
        var ymax = Math.max.apply(Math, vectorNodes.map(function(d){return d.y;}));
        var zmax = Math.max.apply(Math, vectorNodes.map(function(d){return d.z;}));
        var xmin = Math.min.apply(Math, vectorNodes.map(function(d){return d.x;}));
        var ymin = Math.min.apply(Math, vectorNodes.map(function(d){return d.y;}));
        var zmin = Math.min.apply(Math, vectorNodes.map(function(d){return d.z;}));
        //scale functions
        var xScale = d3.scaleLinear()
            .domain([xmin-0.1, xmax+0.1])
            .range([padding, w - padding * 2]);
        var yScale = d3.scaleLinear()
            .domain([ymin-0.1, ymax+0.1])
            .range([h - padding, padding]);
        // smaller Z value -> bigger node
        var zScale = d3.scaleLinear()
            .domain([zmin, zmax])
            .range([10, 3]);
        // smaller Z value -> blue nodes
        var colorScale = d3.scaleLinear()
            .domain([zmin, zmax])
            .range(["blue", "red"]);

        // display axises
        var xAxis = d3.axisBottom().scale(xScale).ticks(10);
        var yAxis = d3.axisLeft().scale(yScale).ticks(10);

        // Sort so smaller nodes get displayed in front of larger nodes
        vectorNodes.sort(function (a, b) {return a.z - b.z;});

        // draw nodes
        svg.selectAll("circle").data(vectorNodes).enter().append("circle")
            .attr("id", function (d) {return "c"+d.id;}) // scale coords properly
            .attr("cx", function (d) {return xScale(d.x);})
            .attr("cy", function(d){return yScale(d.y);})
            .attr("r", function(d){return zScale(d.z);})
            .attr("fill", function (d) {return colorScale(d.z);})
            .attr("stroke","white")
            .on("mouseover", function(d) {
                //ON MOUSEOVER: show label and coords in log
                d3.json("graph/getLabel?id=" + d.id).then( function (jlabel) {
                     document.getElementById(log).innerHTML = "<strong>Label: </strong>"+jlabel.label+"<br/>"+"<strong>x: </strong>"+d.x + "<strong> y :</strong>"+ d.y+"<strong> z :</strong>"+d.z;
                });
                // make hovered node bigger
                d3.selectAll("#c"+d.id).transition().attr("r",function (d) {
                    return zScale(d.z)+10;
                });
            })
            .on("mouseout", function(d) {
                if(isSelected !== d.id){
                    d3.selectAll("#c"+d.id).transition().attr("r", function (d) {
                        return zScale(d.z);
                    });
                }
            })
            .on("click", function(d) {
                // ON CLICK: selects node in both embedding, changes color to green
                if(isSelected != null) {
                    if(isSelected === d.id) {
                        d3.selectAll("#c"+d.id).attr("fill", colorScale(d.z));
                        isSelected = null;
                    } else {
                        d3.selectAll(".c"+isSelected).attr("fill", colorScale(d.z));
                        d3.selectAll("#c"+d.id).attr("fill", "green");
                        isSelected = d.id;
                        d3.selectAll("#c"+d.id).transition().attr("r",function (d) {
                            return zScale(d.z)+5;
                        });
                    }
                } else {
                    d3.selectAll("#c"+d.id).attr("fill", "green");
                    isSelected = d.id;
                }
            });

        //x axis
        svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + (h - padding) + ")")
            .call(xAxis);

        //y axis
        svg.append("g")
            .attr("class", "y axis")
            .attr("transform", "translate(" + padding + ", 0)")
            .call(yAxis);
    }, function (e) {
        throw e;
    });
}

// default view: example compare
compare("12831","761");