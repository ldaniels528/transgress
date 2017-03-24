db.workflows.insert({
    "name": "ListingActivity",
    "input": {
        "name": "listing-activity",
        "type": "file",
        "path": "unknown",
        "format": "psv",
        "columnHeaders": true
    },
    "outputs": [{
        "name": "listing-activity-json",
        "type": "file",
        "path": "./examples/incoming/LISTING_ACTIVITY-{{$date:YYYYMMDD}}.json",
        "format": "json"
    }],
    "onError": {
        "source": {
            "name": "error-output",
            "type": "file",
            "path": "errors.txt",
            "format": "text"
        }
    },
    "variables": []
});

db.workflows.insert({
    "name": "UserAgentsTSV",
    "input": {
        "name": "tsv-input",
        "type": "file",
        "path": "unknown",
        "format": "tsv",
        "columnHeaders": true
    },
    "outputs": [{
        "name": "json-output",
        "type": "file",
        "path": "./example/incoming/useragents-{{$date:YYYYMMDD}}.json",
        "format": "json"
    }],
    "variables": []
});

db.workflows.insert({
    "name": "UserAgentsJSON",
    "input": {
        "name": "json-input",
        "type": "file",
        "path": "unknown",
        "format": "json",
        "columnHeaders": true
    },
    "outputs": [{
        "name": "csv-output",
        "type": "file",
        "path": "./example/incoming/useragents-{{$date:YYYYMMDD}}.csv",
        "format": "csv",
        "columnHeaders": true
    }],
    "variables": []
});

db.workflows.insert({
    "name": "UserAgentsFIXED",
    "input": {
        "name": "csv-input",
        "type": "file",
        "path": "unknown",
        "format": "csv",
        "columnHeaders": true
    },
    "outputs": [
        {
            "name": "fixed-output",
            "type": "file",
            "path": "./example/incoming/useragents-{{$date:YYYYMMDD}}.txt",
            "format": "fixed",
            "fields": [
                {
                    "name": "USER_AGENT_FRAGMENT",
                    "length": 80
                },
                {
                    "name": "USER_AGENT_SOURCE",
                    "length": 12
                },
                {
                    "name": "EFFECTIVE_DATETIME",
                    "length": 12
                }
            ]
        }
    ],
    "variables": []
});