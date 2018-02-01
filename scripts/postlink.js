'use strict';

const xcode = require('xcode');
const fs = require('fs');
const path = require('path');
const packageJson = require('../../../package.json');

// Postlink iOS
const frameworks = [
  'libsqlite3.tbd',
  'libstdc++.6.0.9.tbd',
  'libz.1.2.5.tbd',
  'VideoToolbox.framework',
];

const projectName = packageJson.name;
const projectPath = `./ios/${projectName}.xcodeproj/project.pbxproj`;
const xcodeProject = xcode.project(projectPath);
const pbxProject = xcodeProject.parseSync();
const target = pbxProject.getFirstTarget().uuid;

console.log('Adding frameworks...');
frameworks.map(framework => {
  pbxProject.addFramework(framework, { link: true, target });
  pbxProject.addStaticLibrary(framework, {
    target
  });
});

fs.writeFileSync(
  projectPath,
  pbxProject.writeSync()
);

// Postlink Android
const settingsGradlePath = './android/settings.gradle';
const settingsGradle = fs.readFileSync(settingsGradlePath, 'utf8');
const getIncludeForProject = (name) => (
  `include ':${name}'\n` +
  `project(':${name}').projectDir = ` +
  `new File(rootProject.projectDir, '${path.join(projectDir, 'android', name)}')\n`
);

const projectsToInclude = [
  'zoomcommonlib',
  'zoomsdk'
];
projectsToInclude.map(project => settingsGradle.replace(/\n/, getIncludeForProject(project)));
