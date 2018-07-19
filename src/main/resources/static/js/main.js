
var w = 800;
var h = 600;
var padding = 40;
var colorMain = d3.color("#ff9100"),
    colorNode= d3.color("#6314cc"),
    colorLink= d3.color("#A70110"),
    colorOther= d3.color("#F4F6FF").darker(0.9);

var inputNodeA = document.getElementById("nodeId");
var inputNodeB = document.getElementById("nodeId2");
var inputKInput = document.getElementById("kInput");
var inputmax = document.getElementById("max");

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

function change_action() {
    action = document.getElementById('action').value;
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
        loadGlobalGraph(".box1", "paraVec-3-25.pv", "log1", max);
        loadGlobalGraph(".box2", "deepWalk-400-200-3.dw", "log2", max);
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

function compare(node1, node2) {
    var nodeArgs = node1 + ";" + node2;
    d3.json("http://v122.de:8080/deepWalk/compare?nodeA=" + node1 +"&nodeB=" + node2).then(function (json) {
        document.getElementById("log2").innerHTML = "Similarity: " + json.similar[0].sim;
    });
    d3.json("http://v122.de:8080/paragraphVectors/compare?nodeA=" + node1 +"&nodeB=" + node2).then(function (json) {
        document.getElementById("log1").innerHTML = "Similarity: " + json.similar[0].sim;
    });
    d3.json("http://v122.de:8080/deepWalk/getFriends?label=" + nodeArgs).then(function (json) {
        var n = [];
        var e = [];
        n.push({id:node1, type:"main"});
        n.push({id:node2, type:"main"});
        json[0].friends.forEach(function (l) {
            if(!n.map(function (value) { return value.id }).includes(l)) n.push({id:l, type: "outer"});
            e.push({source: node1, target: l});
        });
        json[1].friends.forEach(function (l) {
            var map = n.map(function (value) { return value.id });
            if(!map.includes(l)) n.push({id:l, type: "outer"});
            else n[map.indexOf(l)] = {id:l, type: "link"};
            e.push({source: node2, target: l});
        });
        n = sortByType(n);
        var graph = {nodes: n, links: e};
        loadLocalGraph(graph, ".box2");
    });
    d3.json("http://v122.de:8080/paragraphVectors/getFeats?label=" + nodeArgs).then(function (json) {
        var n = [];
        var e = [];
        n.push({id:node1, type:"main"});
        n.push({id:node2, type:"main"});
        json[0].feats.forEach(function (l) {
            if(!n.map(function (value) { return value.id }).includes(l)) n.push({id:l, type: "outer"});
            e.push({source: node1, target: l});
        });
        json[1].feats.forEach(function (l) {
            var map = n.map(function (value) { return value.id });
            if(!map.includes(l)) n.push({id:l, type: "outer"});
            else n[map.indexOf(l)] = {id:l, type: "link"};
            e.push({source: node2, target: l});
        });
        n = sortByType(n);
        var graph = {nodes: n, links: e};
        loadLocalGraph(graph, ".box1");
    });
}

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

function topkDeepWalk(id, k, div){
    d3.json("http://v122.de:8080/deepWalk/topK?label="+ id +"&k="+ k).then(function (json) {
        var topKNodes = json.similar;
        var mainNode = json.selection;
        topKNodes.push({label: json.selection, sim: 1});
        var nodeArgs = "";
        topKNodes.forEach(function (n) {
            nodeArgs = nodeArgs + n.label + ";";
        });
        d3.json("http://v122.de:8080/deepWalk/getFriends?label=" + nodeArgs).then(function (friends) {
            var n = [];
            var e = [];
            friends.forEach(function (v) {
                if(v.label === mainNode) n.push({id:v.label, type:"main"});
                else n.push({id:v.label, type:"node"});
                v.friends.forEach(function (l) {
                    if(!n.map(function (value) { return value.id }).includes(l)) n.push({id:l, type: "outer"});
                    e.push({source: v.label, target: l});
                });
            });
            n = sortByType(n);
            var graph = {nodes: n, links: e};
            loadLocalGraph(graph, div)
        });
    });
}

function topkParaVec(id, k, div){
    d3.json("http://v122.de:8080/paragraphVectors/topK?label="+ id +"&k="+ k).then(function (json) {
        var topKNodes = json.similar.map(function (d) {
            return {id: d.label, sim: d.sim, type: "node"}
        });
        var mainNode = json.selection;
        topKNodes.push({id: json.selection, sim: 1, type: "main"});
        var nodeArgs = "";
        topKNodes.forEach(function (d) {
            nodeArgs = nodeArgs + d.id + ";";
        });
        d3.json("http://v122.de:8080/paragraphVectors/getFeats?label=" + nodeArgs).then(function (feats) {
            var n = [];
            var e = [];
            feats.forEach(function (v) {
                if(v.label === mainNode) n.push({id:v.label, type:"main"});
                else n.push({id:v.label, type:"node"});
                v.feats.forEach(function (l) {
                    if (!n.map(function (value) {
                        return value.id
                    }).includes(l)) n.push({id: l, type:"outer"});
                    e.push({source: v.label, target: l});
                });
            });
            n = sortByType(n);
            var graph = {nodes: n, links: e};
            loadLocalGraph(graph, div);
        });
    });
}

function loadLocalGraph(graph, div){
    var svg = d3.select(div)
        .append("svg")
        .attr("width", w)
        .attr("height", h);
    var graphLayout = d3.forceSimulation(graph.nodes)
        .force("charge", d3.forceManyBody().strength(-300))
        .force("center", d3.forceCenter(w / 2, h / 2))
        .force("x", d3.forceX(w / 2).strength(1))
        .force("y", d3.forceY(h / 2).strength(1))
        .force("link", d3.forceLink(graph.links).id(function(d) {return d.id; }).distance(50).strength(1))
        .on("tick", ticked);

    var adjlist = [];
    graph.links.forEach(function(d) {
        adjlist[d.source.index + "-" + d.target.index] = true;
        adjlist[d.target.index + "-" + d.source.index] = true;
    });
    function neigh(a, b) {
        return a == b || adjlist[a + "-" + b];
    }
    var container = svg.append("g");
    svg.call(
        d3.zoom()
            .scaleExtent([.1, 4])
            .on("zoom", function() { container.attr("transform", d3.event.transform); })
    );
    var link = container.append("g").attr("class", "links")
        .selectAll("line")
        .data(graph.links)
        .enter()
        .append("line")
        .attr("stroke", "grey")
        .attr("stroke-width", "1px");
    var node = container.append("g").attr("class", "nodes")
        .selectAll("g")
        .data(graph.nodes)
        .enter().append('g')
        .attr('class', 'nodes');
    node.append("circle")
        .attr("r", function (d) {
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
    node.append("text")
        .attr("dx", 12)
        .attr("dy", ".35em")
        .style("opacity", 0)
        .text(function (d) { return d.id });
    node.on("mouseover", focus).on("mouseout", unfocus);
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

function loadGlobalGraph(div, path, log, max) {
    var vectorNodes = [];
    var isSelected = null;
    var svg = d3.select(div)
        .append("svg")
        .attr("width", w)
        .attr("height", h);

    d3.text(path).then(function (d) {
        var vectors = d.split("\n");
        max = Math.min(vectors.length, max);
        var i;
        for(i = 0; i < max; i++){
            var vec = vectors[i];
            var values = vec.split("\t");
            if(values.length >= 4) vectorNodes.push({id: values[0], x: values[1], y: values[2], z: values[3]});
        }
        var xmax = Math.max.apply(Math, vectorNodes.map(function(d){return d.x;}));
        var ymax = Math.max.apply(Math, vectorNodes.map(function(d){return d.y;}));
        var zmax = Math.max.apply(Math, vectorNodes.map(function(d){return d.z;}));
        var xmin = Math.min.apply(Math, vectorNodes.map(function(d){return d.x;}));
        var ymin = Math.min.apply(Math, vectorNodes.map(function(d){return d.y;}));
        var zmin = Math.min.apply(Math, vectorNodes.map(function(d){return d.z;}));
        //scale function
        var xScale = d3.scaleLinear()
            .domain([xmin-0.1, xmax+0.1])
            .range([padding, w - padding * 2]);
        var yScale = d3.scaleLinear()
            .domain([ymin-0.1, ymax+0.1])
            .range([h - padding, padding]);
        var zScale = d3.scaleLinear()
            .domain([zmin, zmax])
            .range([10, 3]);
        var colorScale = d3.scaleLinear()
            .domain([zmin, zmax])
            .range(["blue", "red"]);

        var xAxis = d3.axisBottom().scale(xScale).ticks(10);
        var yAxis = d3.axisLeft().scale(yScale).ticks(10);

        //Sort so smaller nodes get displayed in front of larger nodes
        vectorNodes.sort(function (a, b) {return a.z - b.z;});

        svg.selectAll("circle").data(vectorNodes).enter().append("circle")
            .attr("id", function (d) {
                return "c"+d.id;
            })
            .attr("cx", function (d) {
                return xScale(d.x);
            })
            .attr("cy", function(d){
                return yScale(d.y);
            })
            .attr("r", function(d){
                return zScale(d.z);
            })
            .attr("fill", function (d) {
                return colorScale(d.z);
            })
            .attr("stroke","white")
            .on("mouseover", function(d) {
                document.getElementById(log).innerHTML = d.id + "<br/>"  + d.x + ", " + d.y + ", " + d.z;
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
compare("12831","761");