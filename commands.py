# Here you can create play commands that are specific to the module, and extend existing commands

import sys
import os
import shutil
import subprocess
import tempfile
from optparse import OptionParser

try:
    from play.utils import package_as_war

    PLAY10 = False
except ImportError:
    PLAY10 = True

MODULE = 'jelastic'

# Commands that are specific to your module

COMMANDS = ['jelastic:deploy']
java_args = []
war_path = None
message = []


class MyOptionParser(OptionParser):
    def error(self, msg):
        pass


def execute(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")
    parser = MyOptionParser()
    parser.add_option("-d", "--domain", dest="domain", help="Domain")
    options, args = parser.parse_args(args)
    app.check()

    jelastic_command = command[command.index(":") + 1:]

    for item in ["domain"]:
        if eval('options.%s' % item) is not None:
            java_args.append("-Djelastic.api.%s=%s" % (item, eval('options.%s' % item)))

    if "jelastic:deploy" in jelastic_command:
        war_path = generate_war(app, env, args)
        java_args.append("-Djelastic.app.war=%s" % war_path)
    java_cmd = app.java_cmd(java_args, None, "play.modules.jelastic.Jelastic", [jelastic_command])
    try:
        print java_args
        subprocess.call(java_cmd, env=os.environ)
    except OSError:
        print "Could not execute the java executable, please make sure the JAVA_HOME environment variable is set properly (the java executable should reside at JAVA_HOME/bin/java). "
        sys.exit(-1)
    print


def generate_war(app, env, args):
    java_cmd = app.java_cmd(args)
    if os.path.exists(os.path.join(app.path, 'tmp')):
        shutil.rmtree(os.path.join(app.path, 'tmp'))
    if os.path.exists(os.path.join(app.path, 'precompiled')):
        shutil.rmtree(os.path.join(app.path, 'precompiled'))
    java_cmd.insert(2, '-Dprecompile=yes')
    try:
        result = subprocess.call(java_cmd, env=os.environ)
        if not result == 0:
            print "~"
            print "~ Precompilation has failed, stop deploying."
            print "~"
            sys.exit(-1)

    except OSError:
        print "Could not execute the java executable, please make sure the JAVA_HOME environment variable is set properly (the java executable should reside at JAVA_HOME/bin/java). "
        sys.exit(-1)

    war_path = os.path.join(tempfile.gettempdir(), os.path.basename(app.path))
    package_as_war(app, env, war_path, "%s.war" % war_path)
    return "%s.war" % war_path

# This will be executed before any command (new, run...)
def before(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")


# This will be executed after any command (new, run...)
def after(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    if command == "new":
        appconf = open(os.path.join(app.path, 'conf/application.conf'), 'a')
        appconf.write("\n")
        appconf.write("# Jelastic Database configuration\n")
        appconf.write("# ~~~~~\n")
        appconf.write("# %jelastic.db=java:/comp/env/jdbc/yourProject\n\n")
        appconf.write("# %jelastic.db.url=jdbc:jelastic://yourDBName\n")
        appconf.write("# %jelastic.db.driver=\n")
        appconf.write("# %jelastic.db.user=\n")
        appconf.write("# %jelastic.db.pass=\n\n")
        appconf.write("# %jelastic.jpa.dialect=org.hibernate.dialect.MySQLDialect\n")
        appconf.write("\n")
        appconf.write("# Jelastic Account configuration\n")
        appconf.write("# ~~~~~\n")
        appconf.write("# %jelastic.user.email=your email key here\n")
        appconf.write("# %jelastic.user.password=your secret password here\n")
