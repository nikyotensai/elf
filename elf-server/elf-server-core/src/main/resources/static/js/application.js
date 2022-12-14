$(document).ready(function () {
    var currentAppCode;

    function getAppList() {
        $('#app-list-table').bootstrapTable('removeAll');
        $.ajax({
            "url": "api/application/list.do",
            "type": "get",
            success: function (ret) {
                if (ret.status === 0) {
                    $('#app-list-table').bootstrapTable('append', ret.data);
                } else {
                    console.log(ret.message)
                    elf.error("应用列表获取失败");
                }
            }
        })
    }

    function getAppServerListByAppCode() {
        $('#app-server-list-table').bootstrapTable('removeAll');
        $.ajax({
            "url": "api/app/server/list.do",
            "type": "get",
            "data": {
                appCode: currentAppCode
            },
            success: function (ret) {
                if (ret.status === 0) {
                    $('#app-server-list-table').bootstrapTable('append', ret.data);
                } else {
                    console.log(ret.message);
                    elf.error(ret.message);
                }
            }
        })
    }

    function getAppOwner(appCode) {
        $.ajax({
            "url": "api/application/owner.do",
            "type": "get",
            data: {
                appCode: appCode
            },
            success: function (ret) {
                if (ret.status === 0) {
                    $("#app-detail-owner").val(ret.data.join("\n"));
                } else {
                    console.log(ret.message);
                    elf.error("应用负责人获取失败");
                }
            }
        })
    }

    function changeAutoJMapHistoEnable(serverId, enable) {
        $.ajax({
            "url": "api/app/server/autoJMapHistoEnable.do",
            "type": "post",
            data: {
                serverId: serverId,
                enable: !enable
            },
            success: function (ret) {
                if (ret.status === 0) {
                    elf.success("修改成功");
                    getAppServerListByAppCode();
                } else {
                    console.log(ret.message);
                    elf.error("自动JMap修改失败");
                }
            }
        })
    }

    function changeAutoJStackEnable(ip, enable) {
        $.ajax({
            "url": "api/app/server/autoJStackEnable.do",
            "type": "post",
            data: {
                ip: ip,
                enable: !enable
            },
            success: function (ret) {
                if (ret.status === 0 && ret.data) {
                    elf.success("修改成功");
                    getAppServerListByAppCode();
                } else {
                    console.log(ret.message);
                    elf.error("自动JStack修改失败");
                }
            }
        })
    }

    function deleteAppServerByServerId(serverId) {
        if (!confirm("确定要删除吗？")) {
            return;
        }
        $.ajax({
            "url": "api/app/server/delete.do",
            "type": "post",
            data: {
                serverId: serverId
            },
            success: function (ret) {
                if (ret.status === 0) {
                    elf.success("删除成功");
                    getAppServerListByAppCode();
                } else {
                    console.log(ret.message);
                    elf.error("删除失败");
                }
            }
        })
    }

    function buildAppDetail(app) {
        $("input[data-role='app-detail-value']").val("");
        $("textarea[data-role='app-detail-value']").val("");
        $("#app-detail-id").val(app.id);
        $("#app-detail-code").val(app.code);
        $("#app-detail-name").val(app.name);
        $("#app-detail-groupCode").val(app.groupCode);
        $("#app-detail-status").val(app.status);
        getAppOwner(app.code);

    }

    function buildAppServerManage(appServer) {
        $("input[data-role='app-server-value']").val("");
        $("#app-server-id").val(appServer.serverId);
        $("#app-server-ip").val(appServer.ip);
        $("#app-server-port").val(appServer.port);
        $("#app-server-host").val(appServer.host);
        $("#app-server-logDir").val(appServer.logDir);
    }

    function initAppListTable() {
        $('#app-list-table').bootstrapTable({
            data: [{}],
            striped: true, //是否显示行间隔色
            pageNumber: 1, //初始化加载第一页
            pagination: true,//是否分页
            sidePagination: 'client',//server:服务器端分页|client：前端分页
            pageSize: 10,//单页记录数
            pageList: [10, 20, 50, 100],//可选择单页记录数
            showRefresh: true,//刷新按钮
            search: true,
            toolbar: "#app-list-toolbar",
            searchAlign: "left",
            buttonsAlign: "left",
            columns: [{
                title: 'code',
                field: 'code',
                sortable: true,
                searchable: true,
            }, {
                title: '名称',
                field: 'name',
                sortable: true,
                searchable: true
            }, {
                title: '组织代号',
                field: 'groupCode',
                sortable: true,
                searchable: true

            }, {
                title: '状态',
                field: 'status',
                sortable: true,
                searchable: false,
                formatter: function (value, row, index) {
                    if (value == 0) {
                        return "<span class='status-unaudit'>未审核</span>"
                    } else if (value == 1) {
                        return "<span class='status-pass'>审核通过</span>";
                    } else if (value == 2) {
                        return "<span class='status-reject'>未通过</span>";
                    } else if (value == 3) {
                        return "<span class='status-discard'>已废弃</span>";
                    } else {
                        return value;
                    }
                }
            }, {
                title: '创建人',
                field: 'creator',
                sortable: true,
                searchable: true
            }, {
                title: '创建时间',
                field: 'createTime',
                sortable: true,
                searchable: false,
                formatter: function (value) {
                    return dateFormat(value);
                }
            }, {
                title: '操作',
                field: 'operate',
                events: operateEvents,
                formatter: function (value, row, index) {
                    return [
                        '<a class="btn btn-info btn-sm app-manage" href="#">查看</a>'
                    ].join('');
                }
            }],
            onRefresh: function () {
                getAppList();
            }
        });
    }


    function initAppServerTable() {
        $('#app-server-list-table').bootstrapTable({
            data: [{}],
            striped: true, //是否显示行间隔色
            pageNumber: 1, //初始化加载第一页
            pagination: true,//是否分页
            sidePagination: 'client',//server:服务器端分页|client：前端分页
            pageSize: 10,//单页记录数
            pageList: [10, 20, 50, 100],//可选择单页记录数
            showRefresh: true,//刷新按钮
            search: true,
            toolbar: '#app-server-toolbar',
            searchAlign: "left",
            buttonsAlign: "left",
            columns: [{
                title: '主机名',
                field: 'host',
                sortable: true,
                searchable: true
            }, {
                title: 'IP',
                field: 'ip',
                sortable: true,
                searchable: true
            }, {
                title: '端口',
                field: 'port',
                sortable: true,
                searchable: false

            }, {
                title: '日志目录',
                field: 'logDir',
                sortable: true,
                searchable: false
            }, {
                title: '线程级cpu监控',
                field: 'autoJStackEnable',
                sortable: true,
                searchable: true,
                events: operateEvents,
                formatter: function (value, row, index) {
                    if (value) {
                        return '<input type="checkbox" class="auto-enable autoJStackEnable" id="autoJStackEnable-' + index + '" name="autoJStackEnable" checked><label for="autoJStackEnable-' + index + '"></label>'
                    } else {
                        return '<input type="checkbox" class="auto-enable autoJStackEnable" id="autoJStackEnable-' + index + '" name="autoJStackEnable"><label for="autoJStackEnable-' + index + '"></label>'
                    }
                }
            }, /*{
                title: '堆对象概览监控',
                field: 'autoJMapHistoEnable',
                sortable: true,
                searchable: true,
                events: operateEvents,
                formatter: function (value, row, index) {
                    if (value) {
                        return '<input type="checkbox" class="auto-enable autoJMapHistoEnable" id="autoJMapHistoEnable-' + index + '" name="autoJMapHistoEnable" checked><label for="autoJMapHistoEnable-' + index + '"></label>'
                    } else {
                        return '<input type="checkbox" class="auto-enable autoJMapHistoEnable" id="autoJMapHistoEnable-' + index + '" name="autoJMapHistoEnable"><label for="autoJMapHistoEnable-' + index + '"></label>'
                    }
                }
            },*/ {
                title: '操作',
                field: 'operate',
                events: operateEvents,
                formatter: function (value, row, index) {
                    return [
                        '<a class="btn btn-info btn-sm app-server-manage" href="#">查看</a>'
                    ].join(' ');
                }
            }],
            onRefresh: function () {
                getAppServerListByAppCode();
            }
        });
    }

    $("#app-server-list-tab").click(function (e) {
        if (currentAppCode) {
            getAppServerListByAppCode();
        } else {
            elf.warning("请先保存应用");
            e.stopPropagation();
        }
    });
    $(".back-app-list").click(function () {
        initPanel();
        getAppList();
    });
    window.operateEvents = {
        "click .app-manage": function (e, value, row, index) {
            currentAppCode = row.code;
            $("#app-detail-tail-app-code").text(currentAppCode);
            $("#app-detail-code").attr("disabled");
            initAppDetailPanel();
            buildAppDetail(row);
        },
        "click .app-server-delete": function (e, value, row, index) {
            deleteAppServerByServerId(row.serverId);
        },
        "click .app-server-manage": function (e, value, row, index) {
            $("#app-server-modal").modal('show');
            buildAppServerManage(row);
        },
        "change .autoJStackEnable": function (e, value, row, index) {
            if (value) {
                changeAutoJStackEnable(row.ip, value);
            } else if (confirm("ELF将每分钟对进行线程级cpu监控")) {
                changeAutoJStackEnable(row.ip, value);
            } else {
                e.currentTarget.checked = value
            }
        },
        "change .autoJMapHistoEnable": function (e, value, row, index) {
            if (value) {
                changeAutoJMapHistoEnable(row.serverId, value)
            } else if (confirm("ELF将每分钟监控堆对象概览，执行jmap -histo操作")) {
                changeAutoJMapHistoEnable(row.serverId, value)
            } else {
                e.currentTarget.checked = value
            }
        }
    }

    function initAppDetailPanel() {
        $("#app-list-panel").hide();
        $("#app-detail-panel").show();
        $("#app-server-list").removeClass("active");
        $("#app-detail").addClass("active");
        $("#app-detail-tab").attr("aria-expanded", true);
        $("#app-server-list-tab").attr("aria-expanded", false);
        $("#app-detail-tab").parent().addClass("active");
        $("#app-server-list-tab").parent().removeClass("active");
    }

    function dateFormat(dateStr) {
        var date = new Date(dateStr)
        var year = date.getFullYear();
        var month = ("0" + (date.getMonth() + 1)).slice(-2);
        var day = ("0" + date.getDate()).slice(-2);
        var h = ("0" + date.getHours()).slice(-2);
        var m = ("0" + date.getMinutes()).slice(-2);
        var s = ("0" + date.getSeconds()).slice(-2);
        return year + "-" + month + "-" + day + " " + h + ":" + m + ":" + s;
    }

    function initPanel() {
        currentAppCode = "";
        $("#app-list-panel").show();
        $("#app-detail-panel").hide();
    }

    function init() {
        initPanel();
        initAppListTable();
        initAppServerTable();
        getAppList();
    }

    init();
})