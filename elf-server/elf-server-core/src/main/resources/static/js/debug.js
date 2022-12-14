$(document).ready(function () {
    var debugResult = "";
    var isReturn = false;
    var keepRunning = false;
    var jarDebug = false;
    var decompilerFile = false;
    var downSourceAllow = false;
    var lineMapping = {};
    var linePrefix = "line";
    var interval = 3000;
    var addBreakpointTime = 0;
    var currentFile;
    var currentClass;
    var currentProject;
    var currentModule;
    var currentBranch;
    var base64 = new Base65();
    var currentHost = {};
    var currentAppCode;
    var currentPointId;
    var currentPoint = {};
    var varSearchResult = [];
    var varSearchResultIndex = 0;
    window.setInterval(function () {
        if (keepRunning) {
            getDebugResult();
        }
    }, interval);


    function relaodClasses() {
        var command = "jardebug -r " + $("#reload-params").val();
        elfWS.sendCommand(currentHost, 9, command, stop, handleResult);
    }

    function getAllClass() {
        var command = "jardebug";
        elfWS.sendCommand(currentHost, 9, command, stop, handleResult);
        // send(currentHost, 9, command);
    }

    function getClassPath(className) {
        var command = "jarclasspath " + encodeURI(className);
        elfWS.sendCommand(currentHost, 9, command, stop, handleResult);
        // send(currentHost, 9, command);
    }

    function decompileClass(className, classPath) {
        var command = "decompilerclass " + encodeURI(className) + " " + encodeURI(classPath);

        elfWS.send(currentHost, 50, command, {className: encodeURI(className), classPath: encodeURI(classPath)}, stop, handleResult);
        // send(currentHost, 50, command);
    }

    function getReleaseInfo() {
        var command = "qdebugreleaseinfo " + currentHost.logDir;
        elfWS.sendCommand(currentHost, 8, command, stop, handleResult);
        // send(currentHost, 8, command);
    }

    function addBreakpoint() {
        //id source line -c condition
        var command = "qdebugadd " + currentPoint.uuid + " " + currentPoint.source + " " + currentPoint.line;
        if (currentPoint.conditional) {
            command += (" -c " + currentPoint.conditional);
        }
        elfWS.sendCommand(currentHost, 8, command, stop, handleResult);
        // send(currentHost, 8, command);
    }

    function deleteBreakPoint() {

        var command = "qdebugremove " + currentPointId;
        elfWS.sendCommand(currentHost, 8, command, stop, handleResult);
        // send(currentHost, 8, command)
    }

    function getDebugResult() {
        debugResult = "";
        var command = "qdebugsearch " + currentPointId;
        elfWS.sendCommand(currentHost, 8, command, stop, handleResult);
        // send(currentHost, 8, command);
    }

    function buildJarDebugPanel(result) {
        var data = result.map(function (item) {
            return {
                "name": item
            }
        });
        $('#jar-debug-table').bootstrapTable('removeAll');
        $('#jar-debug-table').bootstrapTable('append', data);
    }

    function buildDebugResult(result) {
        buildStaticField(result.staticFields);
        buildMemberField(result.fields);
        buildLocalVariables(result.localVariables);
        buildStacktrace(result.stacktrace);
    }

    function buildStaticField(staticFields) {
        $("#static-var tbody").empty();
        if (staticFields) {
            for (var key in staticFields) {
                var value = staticFields[key];
                var tr = $("<tr></tr>");
                var key = $("<td></td>").append(key);
                var value = $("<td></td>").attr("datatype", "value").append($("<pre></pre>").append(getData(value)));
                tr.append(key).append(value).appendTo("#static-var tbody");
            }
        }
    }

    function buildMemberField(fields) {
        $("#member-var tbody").empty();
        if (fields) {
            for (var key in fields) {
                var value = fields[key];
                var tr = $("<tr></tr>");
                var key = $("<td></td>").append(key);
                var value = $("<td></td>").attr("datatype", "value").append($("<pre></pre>").append(getData(value)));
                tr.append(key).append(value).appendTo("#member-var tbody");
            }
        }
    }

    function buildLocalVariables(localVariables) {
        $("#local-var tbody").empty();
        if (localVariables) {
            for (var key in localVariables) {
                if (key == "this") {
                    continue;
                }
                var value = localVariables[key];
                var tr = $("<tr></tr>");
                var key = $("<td></td>").append(key);
                var value = $("<td></td>").attr("datatype", "value").append($("<pre></pre>").append(getData(value)));
                tr.append(key).append(value).appendTo("#local-var tbody");
            }
        }
    }

    function buildStacktrace(result) {
        $("#stack-trace tbody").empty();
        var stacktrace = JSON.parse(result);
        if (stacktrace) {
            stacktrace.forEach(function (value) {
                var tr = $("<tr></tr>");
                var line;
                if (value.lineNumber == -2) {
                    line = $("<td></td>").attr("datatype", "value").append(value.declaringClass + "(Native Method)");
                } else {
                    line = $("<td></td>").attr("datatype", "value").append(value.declaringClass + "(" + value.fileName + ":" + value.lineNumber + ")");
                }
                tr.append(line).appendTo("#stack-trace tbody");
            })
        }
    }

    function buildFilePanel(file) {
        $("#file-path").val("");
        $("#project").val(currentProject);
        $("#branch").val(currentBranch);
        $("#app").val(currentHost.appCode);
        $("#host").val(currentHost.host + ":" + currentHost.port);
        $(".file-panel").show();
        $("#splitter-handle").show();

        decompilerFile = false;
        buildFileContent(file.fileName, base64.decode(file.content))
    }

    function buildDecompilerFileContent(filename, fileContent) {
        $("#file-path").val("");
        $("#project").val(currentProject);
        $("#branch").val(currentBranch);
        $("#app").val(currentHost.appCode);
        $("#host").val(currentHost.host + ":" + currentHost.port);
        $(".file-panel").show();
        $("#splitter-handle").show();

        decompilerFile = true;
        fileContent = base64.decode(fileContent);
        buildFileContent(filename, buildLineMapping(fileContent));
    }

    function buildMavenSourceFileContent(filename, fileContent) {
        $("#file-path").val("");
        $("#project").val(currentProject);
        $("#branch").val(currentBranch);
        $("#app").val(currentHost.appCode);
        $("#host").val(currentHost.host + ":" + currentHost.port);
        $(".file-panel").show();
        $("#splitter-handle").show();

        decompilerFile = false;
        buildFileContent(filename, fileContent);
    }

    function buildFileContent(filename, fileContent) {

        currentFile = filename;

        $(".path-tab").empty();
        $(".path-tab").append($("<li></li>").append(currentFile).addClass("active"));

        $("#code-line").val("");
        $("#conditional-breakpoint").val("");

        $("#file-content-panel").empty();
        var language = getMode(filename);
        var content = hljs.highlight(language, fileContent);
        var codetext = content.value.split("\n");
        var code = $("<code></code>");
        var lineNumberPanel = $("<div></div>").addClass("line-number");
        var codeLinePanel = $("<div></div>").addClass("code-line");
        codetext.forEach(function (value, index) {
            var lineNumber = $("<div></div>").addClass("number").append($("<span></span>").append(index + 1));
            var codeLine = $("<div></div>").addClass("line").append($("<span></span>").addClass("code-content").append(value));
            lineNumber.click(function () {
                if (decompilerFile) {
                    var key = linePrefix + (index + 1);
                    var line = lineMapping[key];
                    if (line != null && line != undefined && line.length > 0) {
                        $("#code-line").val(Number(line[0]));
                    } else {
                        $("#code-line").val("");
                    }
                } else {
                    $("#code-line").val(index + 1);
                }
                $("#file-path").val(currentFile + ":" + (index + 1));
                $(".number").removeClass("selected");
                $(this).addClass("selected")
                $(".line").removeClass("selected")
                codeLine.addClass("selected");
            })
            lineNumber.mouseover(function () {
                $(this).addClass("mouse-over")
                codeLine.addClass("mouse-over");
            })
            lineNumber.mouseout(function () {
                $(".number").removeClass("mouse-over");
                $(".line").removeClass("mouse-over")
            })
            lineNumberPanel.append(lineNumber);
            code.append(codeLine);
        })
        var pre = $("<pre></pre>").append(code)
        codeLinePanel.append(pre);
        $("#file-content-panel").append(lineNumberPanel).append(codeLinePanel);
    }


    function buildLineMapping(fileContent) {
        lineMapping = {};
        var index = fileContent.lastIndexOf("Lines mapping:");
        if (index >= 0) {
            var map = fileContent.substring(index + 15);
            var mapping = map.split("\n");
            mapping.forEach(function (line) {
                if (line != null && line != undefined && line != "") {
                    var lineNumbers = line.split(" <-> ");
                    if (lineNumbers.length == 2) {
                        var key = linePrefix + lineNumbers[1].trim();
                        if (lineMapping[key] == null || lineMapping[key] == undefined) {
                            lineMapping[key] = new Array();
                        }
                        lineMapping[key].push(lineNumbers[0]);
                    }
                }
            })
            fileContent = fileContent.substring(0, index);
        }
        return fileContent;
    }

    function getAppList() {
        currentAppCode = "";
        var select = $("#host-list");
        select.empty();
        select.trigger("chosen:updated");
        $.ajax({
            "url": "/getApps.do",
            "type": "get",
            success: function (ret) {
                if (ret.status === 0) {
                    var select = $("#app-list");
                    select.empty();
                    select.trigger("chosen:updated");
                    select.append($("<option value=''></option>"));
                    ret.data.forEach(function (app) {
                        select.append($("<option></option>").attr("value", app).append(app));
                    })
                    select.chosen({"width": "100%", search_contains: true});
                    select.on('change', function () {
                        var appCode = $("#app-list").val();
                        if (currentAppCode != appCode) {
                            currentAppCode = appCode;
                            getHosts(appCode);
                        }
                    })
                    select.trigger("chosen:updated");
                    $("#app-host-panel").show();
                } else {
                    elf.error('???????????????????????????' + ret.message);
                }
            }

        });
    }

    function getHosts(app) {
        var select = $("#host-list");
        select.empty();
        $.ajax({
            "url": "/getHosts.do",
            "type": "get",
            "data": {
                "appCode": app
            },
            success: function (ret) {
                if (ret.status == 0) {
                    select.append($("<option value=''></option>"))
                    ret.data.forEach(function (machine) {
                        select.append($("<option></option>").attr("value", JSON.stringify(machine)).append(machine.host + ":" + machine.port));
                    })
                    select.on('change', function () {
                        currentHost = JSON.parse($("#host-list").val());
                    })
                    select.chosen({"width": "100%", search_contains: true});
                    select.trigger("chosen:updated");
                } else {
                    elf.error('????????????????????????, ???????????????' + ret.message);
                }
            }
        });
    }

    function stop(type, isReceiveCall) {
        keepRunning = false;
        if (!isReceiveCall) {
            enableBreakPoint();
        }
    }

    function handleResult(content) {
        if (!content) {
            return;
        }
        var result = JSON.parse(content);
        if (!result) {
            return;
        }
        keepRunning = false;
        var resType = result.type;
        if (resType == "qdebugadd") {
            var res = result.data;
            if (res.code == 0) {
                currentPointId = res.data;
                keepRunning = true;
                isReturn = false;
                addBreakpointTime = new Date().getTime();
                elf.success("????????????????????????????????????")
            } else {
                enableBreakPoint();
                keepRunning = false;
                elf.error("??????????????????, " + res.message)
                console.log(res.message);
            }
        } else if (resType == "qdebugremove") {
            var res = result.data;
            if (res.code == 0) {
                keepRunning = false;
                elf.success("??????????????????");
                currentPointId = "";
                enableBreakPoint();
            } else {
                keepRunning = false;
                console.log(res.message);
                elf.error("??????????????????")
            }
        } else if (resType == "qdebugsearch") {
            buildSearchResult(result)
        } else if (resType == "qdebugreleaseinfo") {
            var res = result.data;
            if (res.code == 0) {
                parseCmInfo(res.data);
            } else {
                elf.warning(res.message + "????????????????????????????????????")
                console.log(res.message);
            }
            getAllClass();
        } else if (resType == "jardebug") {
            var res = result.data;
            if (res.code == 0) {
                $("#app-host-panel").hide();
                $("#jar-debug-panel").show();
                jarDebug = true;
                buildJarDebugPanel(res.data)
            } else {
                elf.error(res.message);
                console.log(res.message);
            }
        } else if (resType == "jarclasspath") {
            var res = result.data;
            console.log(res);
            if (res.code == 0) {
                if (res.data && res.data.maven) {
                    getFileFromMaven(res.id, res.data);
                } else {
                    decompileClass(res.id, res.data.classPath);
                }
            } else {
                elf.error(res.message);
                console.log(res.message);
            }
        } else if (resType == "decompilerclass") {
            var res = result.data;
            if (res.code == 0) {
                buildDecompilerFileContent(res.id, res.data);
            } else {
                elf.error(res.message)
                console.log(res.message)
            }
        }
    }

    function buildSearchResult(result) {
        var res = result.data;
        if (res.code == 0) {
            if (!isReturn && currentPointId == res.id) {
                keepRunning = false;
                isReturn = true;
                buildDebugResult(res.data);
                enableBreakPoint();
            } else {
                console.log("?????????????????????????????????")
            }
        } else if (res.code == 2) {
            console.log("?????????????????????????????????????????????")
            keepRunning = true;
        } else if (res.code == 3) {
            console.log(res.message);
            elf.error("????????????????????????")
            enableBreakPoint();
            keepRunning = false;
        } else {
            if (new Date().getTime() - addBreakpointTime > 5000 && currentPointId == res.id) {
                console.log(res.message)
                elf.warning("??????????????????")
                enableBreakPoint();
                keepRunning = false;
            } else {
                keepRunning = true;
            }
        }
    }

    function parseCmInfo(content) {
        $.ajax({
            url: 'api/release/info/parse.do',
            method: 'POST',
            dataType: 'JSON',
            data: {
                content: content
            },
            success: function (res) {
                if (res.status == 0) {
                    var relaeaseInfo = res.data;
                    console.log(relaeaseInfo);
                    currentProject = relaeaseInfo.project;
                    currentModule = relaeaseInfo.module;
                    currentBranch = relaeaseInfo.output;
                } else {
                    elf.warning("release info ???????????????" + res.message + "????????????????????????????????????")
                }
            },
            error: function (error) {
                elf.warning("release info ???????????????" + error.message + "????????????????????????????????????")
            }
        })
    }

    $("#app-host-choose").click(function () {
        if (!currentAppCode) {
            elf.warning("??????????????????")
            return;
        }
        if (!currentHost) {
            elf.warning("??????????????????")
            return;
        }
        getAllClass();
    });

    $(".back-app-host-panel").click(function () {
        $("#jar-debug-panel").hide();
        $("#app-host-panel").show();
    })

    $("#back-class-table").click(function () {
        currentFile = "";
        keepRunning = false;
        cleanDebugResult();
        if (currentPointId && currentPointId != "") {
            deleteBreakPoint();
        }
        enableBreakPoint();
        decompilerFile = false;
        $(".file-panel").hide();
        $("#splitter-handle").hide();
        $("#jar-debug-panel").show();
    })

    $("#classes-reload").click(function () {
        $('#jar-debug-table').bootstrapTable('removeAll');
        relaodClasses();
    })

    $("#add-breakpoint").click(function () {
        if ($("#add-breakpoint").attr("disabled") == "disabled") {
            return;
        }
        cleanDebugResult();

        if (currentFile.lastIndexOf(".java") != currentFile.length - 5) {
            elf.error("?????? Debug ????????? Java ??????");
            return;
        }

        var line = $("#code-line").val();
        var conditional = $("#conditional-breakpoint").val();
        if (line == null || line == undefined || line == "" || line < 0) {
            if (decompilerFile) {
                elf.error("??????????????????????????????????????????????????????");
            } else {
                elf.error("???????????????????????????????????????")
            }
            return;
        } else {
            currentPoint = {
                line: line,
                app: currentHost.appCode,
                host: currentHost.ip,
                source: currentClass.replace(/\./g, "/") + ".java",
                conditional: conditional,
                uuid: uuid()
            }
            disableBreakPoint();
            addBreakpoint();
        }
    })

    $("#delete-breakpoint").click(function () {
        keepRunning = false;
        deleteBreakPoint();
    })

    $("#search-var").click(searchVar);

    $(document).keyup(function (event) {
        if (event.target.nodeName === "BODY" && varSearchResult.length > 0) {
            var preIdx = varSearchResultIndex;
            switch (event.keyCode) {
                // n
                case 78:
                    varSearchResultIndex = ++varSearchResultIndex > varSearchResult.length - 1 ? 0 : varSearchResultIndex;
                    break;
                // p
                case 80:
                    varSearchResultIndex = --varSearchResultIndex < 0 ? varSearchResult.length - 1 : varSearchResultIndex;
                    break;
                default:
                    return;
            }

            varSearchResult[varSearchResultIndex].get(0).scrollIntoView({behavior: "smooth"});
            highlightSearchVar(varSearchResult[preIdx], varSearchResult[varSearchResultIndex]);
        }
    });

    $("#search-var-name").keyup(function (event) {
        if (event.keyCode === 13) {
            $(this).blur();
            searchVar();
        }
    });

    var isResizing = false;
    var lastDownX = 0;
    var flag = 0;
    var width = document.body.clientWidth;
    var isClose = false;
    var tooLeft = false;
    $("#splitter-handle").on("mousedown", function (e) {
        flag = 0;
        isResizing = true;
        lastDownX = e.clientX;
    })
    $(window).resize(function () {
        width = document.body.clientWidth;
    })

    $(document).on("mousemove", function (e) {
        flag = 1;
        if (!isResizing) {
            return;
        }
        var x = e.clientX;
        if (x < width * 0.3) {
            if (!tooLeft) {
                elf.warning("?????????????????????");
                tooLeft = true;
            }
            return;
        } else {
            tooLeft = false;
        }
        if (width - x < 300 && lastDownX < x) {
            closeDebugResultPanel();
        } else {
            isClose = false;
            $("#debug-result-panel").show();
            var codeWidth = x / width * 100 + "%";
            $("#code-panel").css("width", codeWidth);
            $("#file-content-panel").css("width", codeWidth);
            $("#splitter-handle").css("margin-left", codeWidth).attr("title", "???????????????");
            $("#debug-result-panel").css("margin-left", codeWidth).css("width", (1 - x / width) * 100 + "%");
        }
        lastDownX = x;
    })
    $(document).on('mouseup', function (e) {
        isResizing = false;
        if (flag == 0) {
            if (!isClose) {
                closeDebugResultPanel();
            } else {
                openDebugResultPanel();
            }
        }
        var w = $("#debug-result-panel").css("width").replace("px", "");
        if (w < 300) {
            closeDebugResultPanel();
        }
    })

    function searchVar() {
        searchVarName = $('#search-var-name').val().toLowerCase();
        if (searchVarName.trim() === "") {
            elf.warning("??????????????????????????????");
            return;
        }

        cleanVarSearchResult();
        $('#static-var, #member-var, #local-var').each(function () {
            $(this).children('tbody').children().each(function () {
                td = $(this).children().first();
                if (td.text().toLowerCase().indexOf(searchVarName) > -1) {
                    varSearchResult.push(td);
                }
            });
        });

        if (varSearchResult.length === 0) {
            elf.warning('??????????????????[' + searchVarName + ']?????????');
        } else {
            varSearchResult[varSearchResultIndex].get(0).scrollIntoView({behavior: "smooth"});
            highlightSearchVar(null, varSearchResult[varSearchResultIndex]);
        }
    }

    function highlightSearchVar(pre, cur) {
        if (pre) {
            $(pre).removeClass("bg-warning");
        }
        if (cur) {
            $(cur).addClass("bg-warning");
        }
    }

    function cleanVarSearchResult() {
        $(varSearchResult[varSearchResultIndex]).removeClass("bg-warning");
        varSearchResultIndex = 0;
        varSearchResult = [];
    }

    function closeDebugResultPanel() {
        isClose = true;
        var x = width;
        $("#debug-result-panel").hide();
        var codeWidth = x / width * 100 + "%";
        $("#code-panel").css("width", codeWidth);
        $("#file-content-panel").css("width", codeWidth);
        $("#splitter-handle").css("margin-left", codeWidth).attr("title", "???????????????");
    }

    function openDebugResultPanel() {
        isClose = false;
        var x = width * 0.6;
        $("#debug-result-panel").show();
        var codeWidth = x / width * 100 + "%";
        $("#code-panel").css("width", codeWidth);
        $("#file-content-panel").css("width", codeWidth);
        $("#splitter-handle").css("margin-left", codeWidth).attr("title", "???????????????");
        $("#debug-result-panel").css("margin-left", codeWidth).css("width", (1 - x / width) * 100 + "%");
    }

    function cleanDebugResult() {
        $("#debug-result table td[datatype='value']").html("");
        $("#debug-result table tbody").empty();
        $("#search-var-name").val("");
        cleanVarSearchResult();
    }

    function disableBreakPoint() {
        $("#conditional-breakpoint").attr("disabled", true)
        $("#add-breakpoint").attr("disabled", true)
        $("#delete-breakpoint").show();
    }

    function enableBreakPoint() {
        $("#conditional-breakpoint").attr("disabled", false)
        $("#add-breakpoint").attr("disabled", false)
        $("#delete-breakpoint").hide();
        currentPointId = "";
    }

    function init() {
        getAppList();
        initJarDebugTable();
    };

    function initJarDebugTable() {
        $('#jar-debug-table').bootstrapTable({
            data: [{}],
            striped: true, //????????????????????????
            pageNumber: 1, //????????????????????????
            pagination: true,//????????????
            sidePagination: 'client',//server:??????????????????|client???????????????
            pageSize: 10,//???????????????
            pageList: [10, 20, 50, 100],//????????????????????????
            showRefresh: true,//????????????
            search: true,
            toolbar: "#toolbar",
            searchAlign: "left",
            buttonsAlign: "left",
            columns: [{
                title: 'Class',
                field: 'name',
                sortable: true,
                searchable: true
            }, {
                title: "??????",
                field: "operate",
                events: operateEvents,
                formatter: function () {
                    return "<a class='jar-class-debug'>??????</a>"
                }
            }],
            onRefresh: function () {
                $('#jar-debug-table').bootstrapTable('removeAll');
                getAllClass();
            }
        });
    }

    window.operateEvents = {
        "click .jar-class-debug": function (e, value, row, index) {
            currentClass = row.name;
            //????????????????????????????????????
            downSourceAllow = false;
            $("#down-source").hide();
            $("#down-source").unbind("click");

            if (currentProject && currentBranch) {
                getFile(currentProject, currentBranch, currentModule, currentClass, function () {
                    getClassPath(currentClass)
                })
            } else {
                getClassPath(currentClass);
            }

        }
    }

    function uuid() {
        return UUID.prototype.createUUID()
    }

    Array.prototype.remove = function (val) {
        var index = this.lastIndexOf(val);
        if (index > -1) {
            this.splice(index, 1);
        }
    };

    function getData(str) {
        if (!str) {
            return "";
        }
        str = str.replace(/</g, "&lt;").replace(/>/g, "&gt;");
        var isJson = isJsonData(str);
        if (isJson) {
            return syntaxHighlightJsonData(JSON.parse(str));
        } else {
            return str;
        }

    }

    function isJsonData(str) {
        try {
            if (typeof JSON.parse(str) == "object") {
                return true;
            }
        } catch (e) {
            return false;
        }
        return false;
    }

    function syntaxHighlightJsonData(json) {
        if (typeof json != 'string') {
            json = JSON.stringify(json, undefined, 4);
        }
        json = json.replace(/&/g, '&').replace(/</g, '<').replace(/>/g, '>');
        return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
            var cls = 'hljs-number';
            if (/^"/.test(match)) {
                if (/:$/.test(match)) {
                    cls = 'hljs-attr';
                } else {
                    cls = 'hljs-string';
                }
            } else if (/true|false/.test(match)) {
                cls = 'hljs-literal';
            } else if (/null/.test(match)) {
                cls = 'null';
            }
            return '<span class="' + cls + '">' + match + '</span>';
        });
    }

    var EXT_MODE_MAPPINGS = {
        "m": "objectivec",
        "mm": "objectivec",
        "scala": "scala",
        "sql": "sql",
        "xml": "xml",
        "md": "markdown",
        "makefile": "makefile",
        "perl ": "perl",
        "pl ": "perl",
        "java": "java",
        "csh": "bash",
        "ksh": "bash",
        "sh": "shell",
        "json": "json",
        "ini": "ini",
        "css": "css",
        "scss": "scss",
        "sass": "scss",
        "less": "less",
        "ts": "javascript",
        "js": "javascript",
        "rb": "ruby",
        "cs": "cs",
        "coffee": "coffeescript",
        "go": "go",
        "php": "php",
        "cpp": "cpp",
        "properties": "properties",
        "diff": "diff",
        "py": "python",
        "c": "c",
        "html": "xml",
        "groovy": "groovy",
        "gradle": "gradle",
        "swift": "swift"
    };

    function getMode(filename) {
        if (filename) {
            var dotIndex = filename.lastIndexOf('.');
            if (dotIndex == -1) {
                return 'java';
            } else {
                var ext = filename.substr(dotIndex + 1);
                if (EXT_MODE_MAPPINGS[ext]) {
                    return EXT_MODE_MAPPINGS[ext];
                } else {
                    return 'java';
                }
            }
        }
        return 'java';
    }

    init();
})